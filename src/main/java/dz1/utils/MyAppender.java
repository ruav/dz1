package dz1.utils;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Created by ruav on 14.12.16.
 */
public class MyAppender extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent event) {
        System.out.println("It's Sparta!!!");
    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
