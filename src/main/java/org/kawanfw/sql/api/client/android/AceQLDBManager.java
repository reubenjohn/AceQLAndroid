package org.kawanfw.sql.api.client.android;

import android.os.AsyncTask;

import com.aspirephile.shared.debug.Logger;
import com.aspirephile.shared.debug.NullPointerAsserter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AceQLDBManager {
    private static String username;
    private static String password;
    public static BackendConnection remoteConnection;
    private static Logger l = new Logger(AceQLDBManager.class);
    private static NullPointerAsserter asserter = new NullPointerAsserter(l);
    private static String backendUrl;

    public static void initialize(String backendUrl, String username, String password) {
        AceQLDBManager.backendUrl = backendUrl;
        AceQLDBManager.username = username;
        AceQLDBManager.password = password;
    }

    public static void getDefaultRemoteConnectionIfExists(OnRemoteConnectionEstablishedListener listener) {
        getRemoteConnection(backendUrl, listener);
    }

    public static void getRemoteConnection(final String backendUrl, final OnRemoteConnectionEstablishedListener listener) {
        AceQLDBManager.backendUrl = backendUrl;
        if (asserter.assertPointerQuietly(remoteConnection))
            //Send cached remote connection if available
            listener.onRemoteConnectionEstablishedListener(remoteConnection, null);
        else {
            new GetRemoteConnectionTask().execute(listener);
        }
    }

    public static void getResultSet(final OnGetPreparedStatementListener onGetPreparedStatementListener, final OnGetResultSetListener onGetResultSetListener) {
        AceQLDBManager.getDefaultRemoteConnectionIfExists(new OnRemoteConnectionEstablishedListener() {
            @Override
            public void onRemoteConnectionEstablishedListener(final BackendConnection remoteConnection, SQLException e) {
                if (e != null) {
                    onGetResultSetListener.onGetResultSet(null, e);
                } else {
                    ExecuteQueryTask executeQueryTask = new ExecuteQueryTask();
                    executeQueryTask.setPreparedStatement(onGetPreparedStatementListener.onGetPreparedStatementListener(remoteConnection));
                    executeQueryTask.execute(onGetResultSetListener);
                }
            }
        });
    }

    public static <T> void getRowList(final String sql, final ItemBuilder<T> itemBuilder, final OnQueryComplete<T> onQueryComplete) {
        getResultSet(new OnGetPreparedStatementListener() {
            public PreparedStatement onGetPreparedStatementListener(BackendConnection remoteConnection) {
                try {
                    return remoteConnection.prepareStatement(sql);
                } catch (SQLException e) {
                    onQueryComplete.onQueryComplete(null, e);
                    return null;
                }
            }
        }, new OnGetResultSetListener() {
            @Override
            public void onGetResultSet(ResultSet rs, SQLException e) {
                if (e != null) {
                    onQueryComplete.onQueryComplete(null, e);
                } else {
                    try {
                        ArrayList<T> pointList = new ArrayList<>();
                        while (rs.next()) {
                            pointList.add(itemBuilder.buildItem(rs));
                        }
                        rs.close();
                        onQueryComplete.onQueryComplete(pointList, null);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        onQueryComplete.onQueryComplete(null, e1);
                    }
                }
            }
        });
    }

    private static class GetRemoteConnectionTask extends AsyncTask<OnRemoteConnectionEstablishedListener, Void, RemoteConnectionEstablishedResult> {
        private OnRemoteConnectionEstablishedListener listener;

        @Override
        protected RemoteConnectionEstablishedResult doInBackground(OnRemoteConnectionEstablishedListener... listeners) {
            listener = listeners[0];
            try {
                Connection connection = BackendConnection
                        .remoteConnectionBuilder(backendUrl, username, password);
                BackendConnection newRemoteConnection = new BackendConnection(
                        connection);
                if (asserter.assertPointer(newRemoteConnection)) {
                    //Close the old connection if it exists
                    if (remoteConnection != null)
                        remoteConnection.close();
                }
                //Update remote connection even if it is null as this may be intentional.
                remoteConnection = newRemoteConnection;
                return new RemoteConnectionEstablishedResult(remoteConnection, null);
            } catch (SQLException e) {
                return new RemoteConnectionEstablishedResult(null, e);
            }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(RemoteConnectionEstablishedResult result) {
            listener.onRemoteConnectionEstablishedListener(result.remoteConnection, result.sqlException);
        }
    }
}
