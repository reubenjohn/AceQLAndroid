package org.kawanfw.sql.api.client.android;

import android.os.AsyncTask;
import android.util.Log;

import org.kawanfw.sql.api.client.android.execute.OnPrepareStatements;
import org.kawanfw.sql.api.client.android.execute.query.ExecuteQueryTask;
import org.kawanfw.sql.api.client.android.execute.query.ItemBuilder;
import org.kawanfw.sql.api.client.android.execute.query.OnGetResultSetListener;
import org.kawanfw.sql.api.client.android.execute.query.OnQueryComplete;
import org.kawanfw.sql.api.client.android.execute.update.ExecuteUpdateTask;
import org.kawanfw.sql.api.client.android.execute.update.OnUpdateCompleteListener;
import org.kawanfw.sql.api.client.android.execute.update.SQLEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AceQLDBManager {
    //TODO Make private
    public static BackendConnection remoteConnection;
    private static String username;
    private static String password;
    private static String backendUrl;

    public static void initialize(String backendUrl, String username, String password) {
        if (backendUrl != null)
            AceQLDBManager.backendUrl = backendUrl;
        if (username != null)
            AceQLDBManager.username = username;
        if (password != null)
            AceQLDBManager.password = password;
        remoteConnection = null;
    }

    public static String getServerUrl() {
        return backendUrl;
    }

    public static void getRemoteConnection(final String backendUrl, final OnRemoteConnectionEstablishedListener listener) {
        AceQLDBManager.backendUrl = backendUrl;
        if (remoteConnection != null)
            //Send cached remote connection if available
            listener.onRemoteConnectionEstablishedListener(remoteConnection, null);
        else {
            new GetRemoteConnectionTask().execute(listener);
        }
    }

    public static void getDefaultRemoteConnectionIfExists(OnRemoteConnectionEstablishedListener listener) {
        getRemoteConnection(backendUrl, listener);
    }

    public static void executeQuery(final OnPrepareStatements onPrepareStatements, final OnGetResultSetListener onGetResultSetListener) {
        AceQLDBManager.getDefaultRemoteConnectionIfExists(new OnRemoteConnectionEstablishedListener() {
            @Override
            public void onRemoteConnectionEstablishedListener(final BackendConnection remoteConnection, SQLException e) {
                if (e != null) {
                    onGetResultSetListener.onQueryComplete(null, e);
                } else {
                    PreparedStatement[] preparedStatements = onPrepareStatements.onGetPreparedStatementListener(remoteConnection);
                    if (preparedStatements != null) {
                        ExecuteQueryTask executeQueryTask = new ExecuteQueryTask();
                        executeQueryTask.setOnGetResultListener(onGetResultSetListener);
                        executeQueryTask.execute(preparedStatements);
                    } else {
                        onGetResultSetListener.onQueryComplete(null, new SQLException("Null prepared statement"));
                    }
                }
            }
        });
    }

    public static void executeUpdate(final OnPrepareStatements onPrepareStatements, final OnUpdateCompleteListener onUpdateCompleteListener) {
        AceQLDBManager.getDefaultRemoteConnectionIfExists(new OnRemoteConnectionEstablishedListener() {
            @Override
            public void onRemoteConnectionEstablishedListener(final BackendConnection remoteConnection, SQLException e) {
                if (e != null) {
                    onUpdateCompleteListener.onUpdateComplete(null, e);
                } else {
                    PreparedStatement[] preparedStatements = onPrepareStatements.onGetPreparedStatementListener(remoteConnection);
                    if (preparedStatements == null) {
                        onUpdateCompleteListener.onUpdateComplete(null, new SQLException("Null prepared statement"));
                    } else if (preparedStatements.length == 0) {
                        Log.e("AceQLDBManager", "Cannot execute update: Received prepared statement list of length 0");
                    } else {
                        ExecuteUpdateTask executeQueryTask = new ExecuteUpdateTask();
                        executeQueryTask.setOnGetResultListener(onUpdateCompleteListener);
                        executeQueryTask.execute(preparedStatements);
                    }
                }
            }
        });
    }

    public static <T> void getSelectedLists(final String selectStatement, final ItemBuilder<T> itemBuilder, final OnQueryComplete<T> onQueryComplete) {
        executeQuery(new OnPrepareStatements() {
            public PreparedStatement[] onGetPreparedStatementListener(BackendConnection remoteConnection) {
                try {
                    return new PreparedStatement[]{remoteConnection.prepareStatement(selectStatement)};
                } catch (SQLException e) {
                    onQueryComplete.onQueryComplete(null, e);
                    return null;
                }
            }
        }, new OnGetResultSetListener() {
            @Override
            public void onQueryComplete(ResultSet[] resultSets, SQLException e) {
                if (e != null) {
                    onQueryComplete.onQueryComplete(null, e);
                } else {
                    try {
                        ArrayList<List<T>> lists = new ArrayList<>();
                        for (ResultSet rs : resultSets) {
                            ArrayList<T> list = new ArrayList<>();
                            while (rs.next()) {
                                list.add(itemBuilder.buildItem(rs));
                            }
                            rs.close();
                            lists.add(list);
                        }
                        //noinspection unchecked
                        onQueryComplete.onQueryComplete(lists, null);
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        onQueryComplete.onQueryComplete(null, e1);
                    }
                }
            }
        });
    }

    public static <T extends SQLEntity> void insertSQLEntityList(final List<T> list, final OnUpdateCompleteListener onUpdateCompleteListener) {
        if (list.isEmpty()) {
            onUpdateCompleteListener.onUpdateComplete(null, new SQLException("List to be inserted cannot be empty!"));
        } else {
            executeUpdate(new OnPrepareStatements() {
                @Override
                public PreparedStatement[] onGetPreparedStatementListener(BackendConnection remoteConnection) {
                    String entityName = list.get(0).getEntityName();
                    String[] entityAttributeNames = list.get(0).getAttributeNames();

                    StringBuilder statementBuilder = new StringBuilder("insert into " + entityName + " (");
                    for (String attribute : entityAttributeNames) {
                        statementBuilder.append(attribute);
                        statementBuilder.append(',');
                    }
                    statementBuilder.deleteCharAt(statementBuilder.length() - 1);
                    statementBuilder.append(") ");

                    StringBuilder valuesClauseBuilder = new StringBuilder("values(");
                    for (String ignored : entityAttributeNames) {
                        valuesClauseBuilder.append("?,");
                    }
                    valuesClauseBuilder.deleteCharAt(valuesClauseBuilder.length() - 1);
                    valuesClauseBuilder.append("),");
                    String valuesClause = valuesClauseBuilder.toString();

                    for (int i = 0; i < list.size(); i++) {
                        statementBuilder.append(valuesClause);
                    }
                    statementBuilder.deleteCharAt(statementBuilder.length() - 1);

                    try {
                        PreparedStatement preparedStatement = remoteConnection.prepareStatement(statementBuilder.toString());
                        int i = 1;
                        for (T sqlEntity : list) {
                            int newIndex = sqlEntity.onPrepareStatement(preparedStatement, i);
                            if ((newIndex - i) != entityAttributeNames.length) {
                                onUpdateCompleteListener.onUpdateComplete(null, new SQLException("Parameter index was not incremented by the number of attributes during PreparedStatement preparation callback: " +
                                        "Expected " + entityAttributeNames.length + " got " + (newIndex - i)));
                                return new PreparedStatement[0];
                            }
                            i += entityAttributeNames.length;
                        }
                        return new PreparedStatement[]{preparedStatement};
                    } catch (SQLException e) {
                        e.printStackTrace();
                        onUpdateCompleteListener.onUpdateComplete(null, e);
                        return new PreparedStatement[0];
                    }
                }
            }, onUpdateCompleteListener);
        }
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
                //Close the old connection if it exists
                if (remoteConnection != null)
                    remoteConnection.close();
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
