package com.continuuity.common.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing time strings into timestamps, with support for some basic time math.
 * Math syntax includes addition and subtraction in minutes, hours, and days.
 * The "NOW" keyword translates to the current epoch time in seconds, and can be strung together with
 * various additions and subtractions.  For example, "NOW" will translate into the current epoch time in seconds.
 * "NOW-5SECONDS" is 5 seconds before now, "NOW-1DAY" is one day before now, and "NOW-1DAY+4HOURS" is 20 hours
 * before now.
 *
 */
public class TimeMathParser {

  private static final String VALID_UNITS = "DAYS|HOURS|MINUTES|SECONDS";
  private static final Pattern OP_PATTERN = Pattern.compile("(\\-|\\+)(\\d+)(" + VALID_UNITS + ")");
  private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^(\\d+)$");

  public static long nowInSeconds() {
    return TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
  }

  public static long parseTime(String timeStr) {
    return parseTime(nowInSeconds(), timeStr);
  }

  public static long parseTime(long now, String timeStr) {
    Preconditions.checkNotNull(timeStr);

    if ("NOW".equals(timeStr.toUpperCase())) {
      return now;
    }
    // if its a timestamp in seconds
    Matcher matcher = TIMESTAMP_PATTERN.matcher(timeStr);
    if (matcher.matches()) {
      return Integer.parseInt(timeStr);
    }

    // if its some time math pattern like NOW-1DAY-6HOURS
    long output = now;
    if (timeStr.toUpperCase().startsWith("NOW")) {
      matcher = OP_PATTERN.matcher(timeStr);
      // start at 3 to take into account the NOW at the start of the string
      int prevEndPos = 3;
      while (matcher.find()) {
        // happens if there are unexpected things in-between, like "NOW 2HOURS-1MINUTE"
        if (matcher.start() != prevEndPos) {
          throw new IllegalArgumentException("invalid time format " + timeStr);
        }
        String operation = matcher.group(1);
        // group 2 is the number of units, and group 3 is the unit.  ex: 6HOURS
        long offset = TimeUnit.valueOf(matcher.group(3)).toSeconds(Long.parseLong(matcher.group(2)));
        if ("+".equals(operation)) {
          output += offset;
        } else {
          output -= offset;
        }
        prevEndPos = matcher.end();
      }
      // happens if the end of the string is invalid, like "NOW-6HOURS 30MINUTES"
      if (prevEndPos != timeStr.length()) {
        throw new IllegalArgumentException("invalid time format " + timeStr);
      }
    } else {
      throw new IllegalArgumentException("invalid time format " + timeStr);
    }
    return output;
  }
}
