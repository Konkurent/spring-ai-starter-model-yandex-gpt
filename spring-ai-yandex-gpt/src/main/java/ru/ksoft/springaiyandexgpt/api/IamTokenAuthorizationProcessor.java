package ru.ksoft.springaiyandexgpt.api;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;


/**
 * Sends an IAM access token using the {@code Bearer} scheme in the {@code Authorization} header.
 * <p>
 * You can supply the token directly or read it from a file. When a file path is configured, a
 * background task periodically reloads the token from disk so long-lived processes can refresh
 * credentials without a restart. {@link #destroy()} shuts down that scheduler on container shutdown.
 */
@Slf4j
public final class IamTokenAuthorizationProcessor implements AuthorizationProcessor {

    private volatile String token;

    private final StampedLock lock = new StampedLock();

    private final static String PREFIX = "Bearer";

    private final ScheduledExecutorService scheduler;

    /**
     * @param iamOptions IAM token source and optional refresh schedule; must not be {@code null}
     */
    public IamTokenAuthorizationProcessor(AuthOptions.IamOptions iamOptions) {
        Assert.notNull(iamOptions, "IAM options must not be null");
        this.token = iamOptions.getToken();
        if (token == null && iamOptions.getTokenFile() == null) {
            throw new IllegalArgumentException("Only one of token or sourcePath should be provided");
        }



        if (iamOptions.getTokenFile() != null) {
            this.scheduler = Executors.newScheduledThreadPool(1, r -> {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("iam-token-renewer");
                return thread;
            });

            RenewIamTokenTask renewIamTokenTask = new RenewIamTokenTask(
                    iamOptions.getTokenFile(),
                    iamOptions.getInterval(),
                    iamOptions.getIntervalUnit()
            );

            if (token == null) {
                renewIamTokenTask.run();
            } else {
                scheduler.schedule(renewIamTokenTask, iamOptions.getInterval(), iamOptions.getIntervalUnit());
            }
        } else {
            this.scheduler = null;
        }
    }

    @Override
    public void process(HttpHeaders headers) {
        headers.add(HttpHeaders.AUTHORIZATION, PREFIX + " " + readToken());
    }

    private String readToken() {
        long stamp = lock.tryOptimisticRead();
        String internalToken = token;

        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try {
                internalToken = token;
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return internalToken;
    }

    @RequiredArgsConstructor
    private class RenewIamTokenTask implements Runnable {

        private final Path sourcePath;

        private final Long interval;
        private final TimeUnit intervalUnit;

        @Override
        public void run() {
            long stamp = lock.writeLock();
            try {
                String newToken = Files.readString(sourcePath);
                if (newToken.isEmpty()) {
                    log.warn("Token file is empty, keeping previous token...");
                } else {
                    token = newToken;
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            } finally {
                lock.unlockWrite(stamp);
                assert scheduler != null;
                scheduler.schedule(this, interval, intervalUnit);
            }
        }
    }

    /** Stops the background token refresh scheduler when the Spring context shuts down. */
    @PreDestroy
    public void destroy() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

}
