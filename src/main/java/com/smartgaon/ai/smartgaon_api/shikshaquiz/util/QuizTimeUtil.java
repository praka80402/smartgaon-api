package com.smartgaon.ai.smartgaon_api.shikshaquiz.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All quiz timing is anchored to 5:00 AM (Section 4 & 6 of the spec).
 *
 * Rotation slots (fixed, 6/day): 5 AM, 9 AM, 1 PM, 5 PM, 9 PM, 1 AM.
 * Quiz-day: 5:00 AM -> 4:59 AM next calendar day.
 */
public final class QuizTimeUtil {

    public static final int[] SLOT_HOURS = {1, 5, 9, 13, 17, 21};
    public static final int SLOTS_PER_DAY = 6;
    public static final int RESET_HOUR = 5;

    private QuizTimeUtil() {}

    /** 2 AM still belongs to "yesterday" — attempt limits use this, not the calendar date. */
    public static LocalDate getQuizDay(LocalDateTime now) {
        LocalDateTime anchor = now.toLocalDate().atTime(RESET_HOUR, 0);
        if (now.isBefore(anchor)) {
            return now.toLocalDate().minusDays(1);
        }
        return now.toLocalDate();
    }

    /** Next rotation slot strictly after `now`. */
    public static LocalDateTime nextSlotTime(LocalDateTime now) {
        for (int h : SLOT_HOURS) {
            LocalDateTime candidate = now.toLocalDate().atTime(h, 0);
            if (candidate.isAfter(now)) return candidate;
        }
        // past 9 PM -> next slot is 1 AM tomorrow
        return now.toLocalDate().plusDays(1).atTime(SLOT_HOURS[0], 0);
    }

    /** Most recent slot at or before `now`. */
    public static LocalDateTime currentSlotTime(LocalDateTime now) {
        LocalDateTime latest = null;
        for (int h : SLOT_HOURS) {
            LocalDateTime candidate = now.toLocalDate().atTime(h, 0);
            if (!candidate.isAfter(now) && (latest == null || candidate.isAfter(latest))) {
                latest = candidate;
            }
        }
        if (latest == null) {
            // between midnight and 1 AM -> last slot was 9 PM yesterday
            latest = now.toLocalDate().minusDays(1).atTime(21, 0);
        }
        return latest;
    }

    /** Seconds until the next rotation — drives the countdown display (Section 8.3). */
    public static long secondsToNextSlot(LocalDateTime now) {
        return Duration.between(now, nextSlotTime(now)).getSeconds();
    }

    /** Seconds until the next 5 AM reset — used as Redis TTL for attempt counters. */
    public static long secondsToNextReset(LocalDateTime now) {
        LocalDateTime reset = now.toLocalDate().atTime(RESET_HOUR, 0);
        if (!reset.isAfter(now)) reset = reset.plusDays(1);
        return Duration.between(now, reset).getSeconds();
    }

    /** All 6 slot datetimes belonging to a given quiz-day (starts at 5 AM). */
    public static List<LocalDateTime> slotsForQuizDay(LocalDate quizDay) {
        List<LocalDateTime> slots = new ArrayList<>();
        slots.add(quizDay.atTime(5, 0));
        slots.add(quizDay.atTime(9, 0));
        slots.add(quizDay.atTime(13, 0));
        slots.add(quizDay.atTime(17, 0));
        slots.add(quizDay.atTime(21, 0));
        slots.add(quizDay.plusDays(1).atTime(1, 0));
        return slots;
    }

    /** "3h 40m" style countdown text used in the attempt-limit message. */
    public static String formatCountdown(LocalDateTime now) {
        long totalMinutes = Duration.between(now, nextSlotTime(now)).toMinutes();
        long h = totalMinutes / 60;
        long m = totalMinutes % 60;
        if (h > 0) return h + "h " + m + "m";
        return m + "m";
    }

    public static boolean isSlotTimeValid(LocalTime t) {
        for (int h : SLOT_HOURS) {
            if (t.getHour() == h && t.getMinute() == 0) return true;
        }
        return false;
    }
}
