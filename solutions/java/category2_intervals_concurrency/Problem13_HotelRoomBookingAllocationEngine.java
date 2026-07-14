package category2_intervals_concurrency;

import java.util.*;

/**
 * Problem 13: Hotel Room Booking Allocation Engine
 * 
 * Three-part progression:
 * - Part 1: Fix date calculations (handle leap years properly)
 * - Part 2: Merge room occupancy dates to find 100% full nights
 * - Part 3: Find room combination for continuous multi-day stay
 */
public class Problem13_HotelRoomBookingAllocationEngine {

    /**
     * Part 1: Bug Fix - Leap Year Handling
     * Issue: February hardcoded as 28 days, breaks in leap years
     * Solution: Use Java date libraries for proper date handling
     */
    public static class Part1_BugFix {
        public static class DateRange {
            public int year;
            public int month;  // 1-12
            public int day;    // 1-31

            public DateRange(int year, int month, int day) {
                this.year = year;
                this.month = month;
                this.day = day;
            }

            /**
             * Get number of days in month (handles leap years)
             */
            public static int getDaysInMonth(int year, int month) {
                int[] daysPerMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
                
                if (month == 2 && isLeapYear(year)) {
                    return 29;
                }
                return daysPerMonth[month - 1];
            }

            /**
             * Check if year is leap year
             */
            public static boolean isLeapYear(int year) {
                return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
            }

            /**
             * Convert date to day number for comparison
             */
            public long toDayNumber() {
                long days = 0;
                
                // Add days from all previous years
                for (int y = 1970; y < year; y++) {
                    days += isLeapYear(y) ? 366 : 365;
                }
                
                // Add days from previous months
                for (int m = 1; m < month; m++) {
                    days += getDaysInMonth(year, m);
                }
                
                // Add days in current month
                days += day;
                
                return days;
            }

            @Override
            public String toString() {
                return String.format("%04d-%02d-%02d", year, month, day);
            }
        }
    }

    /**
     * Part 2: Occupancy Analysis
     * Find dates when all rooms are booked (100% capacity)
     */
    public static class Part2_OccupancyAnalysis {
        public static class BookingBlock {
            public long startDate;
            public long endDate;
            public String roomId;
            public String guestId;

            public BookingBlock(long startDate, long endDate, String roomId, String guestId) {
                this.startDate = startDate;
                this.endDate = endDate;
                this.roomId = roomId;
                this.guestId = guestId;
            }

            public int getDuration() {
                return (int)(endDate - startDate);
            }
        }

        /**
         * Find nights with 100% occupancy (all rooms booked)
         * Time Complexity: O(n*m) where n = bookings, m = date range
         */
        public static List<Long> findFullOccupancyNights(
            List<BookingBlock> bookings,
            int totalRooms) {
            
            if (bookings == null || bookings.isEmpty()) {
                return new ArrayList<>();
            }

            // Find date range
            long minDate = Long.MAX_VALUE;
            long maxDate = Long.MIN_VALUE;
            
            for (BookingBlock booking : bookings) {
                minDate = Math.min(minDate, booking.startDate);
                maxDate = Math.max(maxDate, booking.endDate);
            }

            // Count rooms booked per date
            Map<Long, Integer> occupancy = new HashMap<>();
            
            for (BookingBlock booking : bookings) {
                for (long date = booking.startDate; date < booking.endDate; date++) {
                    occupancy.put(date, occupancy.getOrDefault(date, 0) + 1);
                }
            }

            // Find fully booked dates
            List<Long> fullNights = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : occupancy.entrySet()) {
                if (entry.getValue() == totalRooms) {
                    fullNights.add(entry.getKey());
                }
            }

            Collections.sort(fullNights);
            return fullNights;
        }
    }

    /**
     * Part 3: Room Combination Finder
     * Find room combination for continuous multi-day stay
     */
    public static class Part3_RoomCombinationFinder {
        /**
         * Find rooms that collectively cover 5-day stay
         * Room can change each day but must be continuous
         */
        public static List<String> findRoomCombination(
            List<Part2_OccupancyAnalysis.BookingBlock> bookings,
            long checkInDate,
            long checkOutDate) {
            
            if (checkInDate >= checkOutDate) {
                return new ArrayList<>();
            }

            List<String> roomCombination = new ArrayList<>();
            
            // For each night in stay
            for (long date = checkInDate; date < checkOutDate; date++) {
                // Find an available room for this night
                boolean found = false;
                
                for (Part2_OccupancyAnalysis.BookingBlock booking : bookings) {
                    // Check if room is booked on this date
                    if (booking.startDate <= date && booking.endDate > date) {
                        // Room is booked
                        continue;
                    }
                    
                    // Room is available
                    roomCombination.add(booking.roomId);
                    found = true;
                    break;
                }
                
                if (!found) {
                    // No room available for this night
                    return new ArrayList<>();
                }
            }
            
            return roomCombination;
        }
    }

    // ============== TEST CASES ==============
    public static void main(String[] args) {
        System.out.println("=== Problem 13: Hotel Room Booking Allocation Engine ===");

        // Part 1: Test leap year handling
        System.out.println("\n--- Part 1: Leap Year Handling ---");
        int[][] testYears = {
            {2000, 2},  // leap year, Feb
            {2001, 2},  // not leap year, Feb
            {2004, 2},  // leap year, Feb
            {2100, 2}   // not leap year (divisible by 100 but not 400)
        };
        for (int[] test : testYears) {
            int days = Part1_BugFix.DateRange.getDaysInMonth(test[0], test[1]);
            System.out.println("  " + test[0] + "-" + test[1] + ": " + days + " days");
        }

        // Part 2: Test occupancy analysis
        System.out.println("\n--- Part 2: Full Occupancy Nights ---");
        List<Part2_OccupancyAnalysis.BookingBlock> bookings = Arrays.asList(
            new Part2_OccupancyAnalysis.BookingBlock(100, 105, "room1", "guest1"),
            new Part2_OccupancyAnalysis.BookingBlock(100, 105, "room2", "guest2"),
            new Part2_OccupancyAnalysis.BookingBlock(102, 104, "room3", "guest3")
        );
        List<Long> fullNights = Part2_OccupancyAnalysis.findFullOccupancyNights(bookings, 3);
        System.out.println("Fully booked nights: " + fullNights);

        // Part 3: Test room combination
        System.out.println("\n--- Part 3: Room Combination for Stay ---");
        List<String> rooms = Part3_RoomCombinationFinder.findRoomCombination(
            bookings, 100, 105);
        System.out.println("Room combination for 5-night stay: " + rooms);
    }
}
