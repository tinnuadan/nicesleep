package de.docm77.patreon.nicesleep;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Utils {
  public static int Round(double val, RoundingMode roundingMode) {
    if (roundingMode == RoundingMode.UNNECESSARY) {
      if (isEqual(Math.floor(val), val)) {
        return (int) Math.ceil(val);
      } else {
        throw new ArithmeticException(String.valueOf(val) + " cannot be casted to an int without rounding");
      }
    }
    if (roundingMode == RoundingMode.CEILING || roundingMode == RoundingMode.UP) {
      return (int) Math.ceil(val);
    }
    if (roundingMode == RoundingMode.FLOOR || roundingMode == RoundingMode.DOWN) {
      return (int) Math.floor(val);
    }

    // rounding mode is HALF_DOWN, HALF_UP or HALF_EVEN
    if (isEqual(Math.floor(val) + 0.5, val)) {
      int lower = (int) Math.floor(val);
      int upper = (int) Math.ceil(val);
      if (roundingMode == RoundingMode.HALF_DOWN) {
        return lower;
      }
      if (roundingMode == RoundingMode.HALF_UP) {
        return upper;
      }
      // RoundingMode.HALF_EVEN
      if (Math.abs(lower) % 2 == 0) {
        return lower;
      } else {
        return upper;
      }
    }
    return (int) Math.round(val);
  }

  public static boolean isEqual(double a, double b) {
    BigDecimal bda = new BigDecimal(a);
    BigDecimal bdb = new BigDecimal(b);
    return bda.compareTo(bdb) == 0;
  }

}