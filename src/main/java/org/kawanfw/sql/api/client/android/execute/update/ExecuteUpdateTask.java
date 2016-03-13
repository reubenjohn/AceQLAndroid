package org.kawanfw.sql.api.client.android.execute.update;

import android.os.AsyncTask;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ExecuteUpdateTask extends AsyncTask<PreparedStatement, Void, ExecuteUpdateResult> {
    private OnUpdateCompleteListener listener;

    @Override
    protected ExecuteUpdateResult doInBackground(PreparedStatement... preparedStatements) {
        int[] updateCounts = new int[preparedStatements.length];
        try {
            for (int i = 0; i < preparedStatements.length; i++) {
                updateCounts[i] = preparedStatements[i].executeUpdate();
                preparedStatements[i].close();
            }
            return new ExecuteUpdateResult(updateCounts, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ExecuteUpdateResult(null, e);
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(ExecuteUpdateResult result) {
        listener.onUpdateComplete(result.updateCounts, result.sqlException);
    }

    public void setOnGetResultListener(OnUpdateCompleteListener onGetResultListener) {
        this.listener = onGetResultListener;
    }
}
