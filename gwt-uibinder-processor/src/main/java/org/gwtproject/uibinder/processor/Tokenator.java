package org.gwtproject.uibinder.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Methods to dispense unique text tokens to be stitched into text, and to help replace the tokens
 * with arbitrary content. Multiple tokenators can be used across the same body of text without fear
 * of the tokens they vend colliding with each other.
 *
 * <p>A arbitrary metadata object ("info") can be associated with each token.
 */
public class Tokenator {

  /**
   * Resolves a token to its literal value.
   */
  public interface Resolver {

    String resolveToken(String token);
  }

  /**
   * Return values for {@link Tokenator#getOrderedValues(String)}.
   */
  public static class ValueAndInfo {

    public final String value;
    public final Object info;

    private ValueAndInfo(String value, Object info) {
      this.value = value;
      this.info = info;
    }
  }

  private static final String TOKEN = "--token--";
  private static final String TOKEN_REGEXP = "\\-\\-token\\-\\-";
  private static int curId = 0;

  public static String detokenate(String betokened, Resolver resolver) {
    StringBuilder detokenated = new StringBuilder();

    int index = 0, nextToken = 0;
    while ((nextToken = betokened.indexOf(TOKEN, index)) > -1) {
      detokenated.append(betokened.substring(index, nextToken));

      int endToken = betokened.indexOf(TOKEN, nextToken + TOKEN.length());
      String token = betokened.substring(nextToken, endToken + TOKEN.length());
      detokenated.append(resolver.resolveToken(token));

      index = endToken + TOKEN.length();
    }

    detokenated.append(betokened.substring(index));
    return detokenated.toString();
  }

  public static boolean hasToken(String s) {
    return s.matches(".*" + TOKEN_REGEXP + "\\d+" + TOKEN_REGEXP + ".*");
  }

  private static String nextToken() {
    return TOKEN + (curId++) + TOKEN;
  }

  private final Map<String, Object> infoMap = new HashMap<String, Object>();

  private Map<String, String> tokenToResolved = new HashMap<String, String>();

  /**
   * Given a string filled with tokens created by {@link #nextToken(Object, String)}, returns it
   * with the tokens replaced by the original strings.
   */
  public String detokenate(String betokened) {
    return detokenate(betokened, new Resolver() {
      public String resolveToken(String token) {
        return tokenToResolved.get(token);
      }
    });
  }

  /**
   * Returns a list of the values represented by tokens in the given string, and the info objects
   * corresponding to them.
   */
  public List<ValueAndInfo> getOrderedValues(String betokened) {
    return getOrderedValues(betokened, new Resolver() {
      public String resolveToken(String token) {
        return tokenToResolved.get(token);
      }
    });
  }

  /**
   * Returns a token that can be used to replace the given String, to be restored by a later call to
   * {@link #detokenate(String)}. Associates the token with the given info object.
   *
   * @param info An arbitrary object to associate with this token. Mmm, metadata
   * @param resolved The value to replace this token with in later calls to {@link
   * #detokenate(String)}
   * @return the token
   */
  public String nextToken(Object info, final String resolved) {
    String token = nextToken();
    tokenToResolved.put(token, resolved);
    infoMap.put(token, info);
    return token;
  }

  /**
   * Like {@link #nextToken(String)} with no info.
   */
  public String nextToken(String resolved) {
    return nextToken(null, resolved);
  }

  private List<ValueAndInfo> getOrderedValues(String betokened, Resolver resolver) {
    List<ValueAndInfo> values = new ArrayList<ValueAndInfo>();

    int index = 0, nextToken = 0;
    while ((nextToken = betokened.indexOf(TOKEN, index)) > -1) {
      int endToken = betokened.indexOf(TOKEN, nextToken + TOKEN.length());
      String token = betokened.substring(nextToken, endToken + TOKEN.length());
      values.add(new ValueAndInfo(resolver.resolveToken(token), infoMap.get(token)));

      index = endToken + TOKEN.length();
    }

    return values;
  }
}
