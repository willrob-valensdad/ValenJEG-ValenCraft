package dev.valen.compat.energy;

import java.math.BigDecimal;
import java.math.BigInteger;

/** Integer transfer quanta avoid round-trip energy creation at fractional configured rates. */
record EnergyRate(double configured, long teamUnits, long neroUnits) {
    static EnergyRate of(double configured) {
        if (!Double.isFinite(configured) || configured <= 0) throw new IllegalArgumentException("Invalid Nero energy ratio");
        BigDecimal decimal = BigDecimal.valueOf(configured).stripTrailingZeros();
        BigInteger numerator = decimal.unscaledValue();
        BigInteger denominator = BigInteger.ONE;
        if (decimal.scale() >= 0) denominator = BigInteger.TEN.pow(decimal.scale());
        else numerator = numerator.multiply(BigInteger.TEN.pow(-decimal.scale()));
        BigInteger divisor = numerator.gcd(denominator);
        return new EnergyRate(configured, numerator.divide(divisor).longValueExact(),
                              denominator.divide(divisor).longValueExact());
    }
    long toTeam(long nero) { return convert(nero, neroUnits, teamUnits); }
    long toNero(long team) { return convert(team, teamUnits, neroUnits); }
    long wholeTeam(long team) { return Math.max(0, team) / teamUnits * teamUnits; }
    long wholeNero(long nero) { return Math.max(0, nero) / neroUnits * neroUnits; }
    private static long convert(long amount, long from, long to) {
        long batches = Math.max(0, amount) / from;
        return Math.min(batches, Long.MAX_VALUE / to) * to;
    }
}
