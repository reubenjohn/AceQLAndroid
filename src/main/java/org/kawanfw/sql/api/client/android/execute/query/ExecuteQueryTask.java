package org.kawanfw.sql.api.client.android.execute.query;

import android.os.AsyncTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExecuteQueryTask extends AsyncTask<PreparedStatement, Void, ExecuteQueryResult> {
    private OnGetResultSetsListener listener;

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

    public void setOnGetResultListener(OnGetResultSetsListener onGetResultListener) {
        this.listener = onGetResultListener;
    }
}
