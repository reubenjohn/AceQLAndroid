package org.kawanfw.sql.api.client.android.execute.update;

import java.sql.SQLException;

/**
 * Created by Reuben John on 3/12/2016.
 */
public class ExecuteUpdateResult {
    final int[] updateCounts;
    final SQLException sqlException;

    public ExecuteUpdateResult(int[] updateCounts, SQLException e) {
        this.updateCounts = updateCounts;
        this.sqlException = e;
    }
}
