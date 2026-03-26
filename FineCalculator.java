public class FineCalculator {
    public static FineResult calculateFine(Book book) {
        if (book == null || book.isAvailable()) {
            return new FineResult(0, 0);
        }

        long daysUsed = java.time.temporal.ChronoUnit.DAYS.between(book.getIssueDate(), java.time.LocalDate.now());
        long lateDays = Math.max(0, daysUsed - AppConfig.MAX_BOOK_ISSUE_DAYS);
        long fineAmount = lateDays * AppConfig.FINE_PER_LATE_DAY;
        return new FineResult(lateDays, fineAmount);
    }

    public static class FineResult {
        private final long lateDays;
        private final long fineAmount;

        public FineResult(long lateDays, long fineAmount) {
            this.lateDays = lateDays;
            this.fineAmount = fineAmount;
        }

        public long getLateDays() { return lateDays; }
        public long getFineAmount() { return fineAmount; }
    }
}
