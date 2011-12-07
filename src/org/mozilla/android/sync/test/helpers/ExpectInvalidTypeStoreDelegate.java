package org.mozilla.android.sync.test.helpers;

import static junit.framework.Assert.assertEquals;

import org.mozilla.android.sync.repositories.InvalidBookmarkTypeException;

public class ExpectInvalidTypeStoreDelegate extends DefaultStoreDelegate {
  
  @Override
  public void onStoreFailed(Exception ex) {
    assertEquals(InvalidBookmarkTypeException.class, ex.getClass());
  }
  
}
