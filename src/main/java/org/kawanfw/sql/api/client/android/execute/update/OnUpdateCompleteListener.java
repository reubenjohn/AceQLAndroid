package org.kawanfw.sql.api.client.android.execute.update;

import java.sql.SQLException;

public interface OnUpdateCompleteListener {
    void onUpdateComplete(int[] results, SQLException e);
}
