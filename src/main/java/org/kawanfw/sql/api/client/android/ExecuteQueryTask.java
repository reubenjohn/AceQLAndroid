package org.kawanfw.sql.api.client.android;

import android.os.AsyncTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExecuteQueryTask extends AsyncTask<OnGetResultSetListener, Void, ExecuteQueryResult> {
    private OnGetResultSetListener listener;
    private PreparedStatement preparedStatement;

    @Override
    protected ExecuteQueryResult doInBackground(OnGetResultSetListener... listeners) {
        listener = listeners[0];
        try {
            ResultSet rs = preparedStatement.executeQuery();
            preparedStatement.close();
            return new ExecuteQueryResult(rs, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ExecuteQueryResult(null, e);
        }
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(ExecuteQueryResult result) {
        listener.onGetResultSet(result.resultSet, result.sqlException);
    }

    public void setPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }
}
