package org.kawanfw.sql.api.client.android;

import android.os.AsyncTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExecutePreparedStatementTask extends AsyncTask<PreparedStatement, Void, ExecuteQueryResult> {
    private OnGetResultSetListener listener;

    @Override
    protected ExecuteQueryResult doInBackground(PreparedStatement... preparedStatements) {
        ResultSet[] resultSets = new ResultSet[preparedStatements.length];
        try {
            for (int i = 0; i < preparedStatements.length; i++) {
                resultSets[i] = preparedStatements[i].executeQuery();
                preparedStatements[i].close();
            }
            return new ExecuteQueryResult(resultSets, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ExecuteQueryResult(null, e);
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(ExecuteQueryResult result) {
        listener.onGetResultSets(result.resultSets, result.sqlException);
    }

    public void setOnGetResultListener(OnGetResultSetListener onGetResultListener) {
        this.listener = onGetResultListener;
    }
}
