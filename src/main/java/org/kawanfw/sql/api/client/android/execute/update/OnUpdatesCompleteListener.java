package org.kawanfw.sql.api.client.android.execute.update;

import java.sql.SQLException;

public interface OnUpdatesCompleteListener {
    void onUpdatesComplete(int[] results, SQLException e);
}
