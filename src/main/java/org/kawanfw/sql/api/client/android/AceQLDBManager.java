package org.kawanfw.sql.api.client.android;

import android.os.AsyncTask;
import android.util.Log;

import org.kawanfw.sql.api.client.android.execute.OnGetPrepareStatement;
import org.kawanfw.sql.api.client.android.execute.OnGetPrepareStatements;
import org.kawanfw.sql.api.client.android.execute.query.ExecuteQueryTask;
import org.kawanfw.sql.api.client.android.execute.query.ItemBuilder;
import org.kawanfw.sql.api.client.android.execute.query.OnGetResultSetListener;
import org.kawanfw.sql.api.client.android.execute.query.OnGetResultSetsListener;
import org.kawanfw.sql.api.client.android.execute.query.OnQueryComplete;
import org.kawanfw.sql.api.client.android.execute.update.ExecuteUpdateTask;
import org.kawanfw.sql.api.client.android.execute.update.OnUpdateCompleteListener;
import org.kawanfw.sql.api.client.android.execute.update.OnUpdatesCompleteListener;
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

    public static void executeQueries(final OnGetPrepareStatements onGetPrepareStatements, final OnGetResultSetsListener onGetResultSetsListener) {
        AceQLDBManager.getDefaultRemoteConnectionIfExists(new OnRemoteConnectionEstablishedListener() {
            @Override
            public void onRemoteConnectionEstablishedListener(final BackendConnection remoteConnection, SQLException e) {
                if (e != null) {
                    onGetResultSetsListener.onGetResultSets(null, e);
                } else {
                    PreparedStatement[] preparedStatements = onGetPrepareStatements.onGetPreparedStatements(remoteConnection);
                    if (preparedStatements != null) {
                        ExecuteQueryTask executeQueryTask = new ExecuteQueryTask();
                        executeQueryTask.setOnGetResultListener(onGetResultSetsListener);
                        executeQueryTask.execute(preparedStatements);
                    } else {
                        onGetResultSetsListener.onGetResultSets(null, new SQLException("Null prepared statement"));
                    }
                }
            }
        });
    }

    public static void executeQuery(final OnGetPrepareStatement onGetPrepareStatement, final OnGetResultSetListener onGetResultSetListener) {
        executeQueries(new OnGetPrepareStatements() {
            @Override
            public PreparedStatement[] onGetPreparedStatements(BackendConnection remoteConnection) {
                return new PreparedStatement[]{onGetPrepareStatement.onGetPreparedStatement(remoteConnection)};
            }
        }, new OnGetResultSetsListener() {
            @Override
            public void onGetResultSets(ResultSet[] rs, SQLException e) {
                onGetResultSetListener.onGetResultSet(rs == null ? null : rs[0], e);
            }
        });
    }

    public static void executeUpdates(final OnGetPrepareStatements onGetPrepareStatements, final OnUpdatesCompleteListener onUpdatesCompleteListener) {
        AceQLDBManager.getDefaultRemoteConnectionIfExists(new OnRemoteConnectionEstablishedListener() {
            @Override
            public void onRemoteConnectionEstablishedListener(final BackendConnection remoteConnection, SQLException e) {
                if (e != null) {
                    onUpdatesCompleteListener.onUpdatesComplete(null, e);
                } else {
                    PreparedStatement[] preparedStatements = onGetPrepareStatements.onGetPreparedStatements(remoteConnection);
                    if (preparedStatements == null) {
                        onUpdatesCompleteListener.onUpdatesComplete(null, new SQLException("Null prepared statement"));
                    } else if (preparedStatements.length == 0) {
                        Log.e("AceQLDBManager", "Cannot execute update: Received prepared statement list of length 0");
                    } else {
                        ExecuteUpdateTask executeUpdateTask = new ExecuteUpdateTask();
                        executeUpdateTask.setOnGetResultListener(onUpdatesCompleteListener);
                        executeUpdateTask.execute(preparedStatements);
                    }
                }
            }
        });
    }

    public static void executeUpdate(final OnGetPrepareStatement onGetPrepareStatement, final OnUpdateCompleteListener onUpdateCompleteListener) {
        executeUpdates(new OnGetPrepareStatements() {
            @Override
            public PreparedStatement[] onGetPreparedStatements(BackendConnection remoteConnection) {
                return new PreparedStatement[]{onGetPrepareStatement.onGetPreparedStatement(remoteConnection)};
            }
        }, new OnUpdatesCompleteListener() {
            @Override
            public void onUpdatesComplete(int[] results, SQLException e) {
                onUpdateCompleteListener.onUpdateComplete(results.length > 0 ? results[0] : 0, e);
            }
        });
    }

    public static <T> void getSelectedLists(final String selectStatement, final ItemBuilder<T> itemBuilder, final OnQueryComplete<T> onQueriesComplete) {
        executeQuery(new OnGetPrepareStatement() {
            public PreparedStatement onGetPreparedStatement(BackendConnection remoteConnection) {
                try {
                    return remoteConnection.prepareStatement(selectStatement);
                } catch (SQLException e) {
                    onQueriesComplete.onQueryComplete(null, e);
                    return null;
                }
            }
        }, new OnGetResultSetListener() {
            @Override
            public void onGetResultSet(ResultSet resultSet, SQLException e) {
                if (e != null) {
                    onQueriesComplete.onQueryComplete(null, e);
                } else {
                    try {
                        if (resultSet != null) {
                            ArrayList<T> list = new ArrayList<>();
                            while (resultSet.next()) {
                                list.add(itemBuilder.buildItem(resultSet));
                            }
                            resultSet.close();
                            //noinspection unchecked
                            onQueriesComplete.onQueryComplete(list, null);
                        } else {
                            onQueriesComplete.onQueryComplete(null, new SQLException("Query did not return a single list"));
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        onQueriesComplete.onQueryComplete(null, e1);
                    }
                }
            }
        });
    }

    public static <T extends SQLEntity> void insertSQLEntityList(final List<T> list, final OnUpdateCompleteListener onUpdateCompleteListener) {
        if (list.isEmpty()) {
            onUpdateCompleteListener.onUpdateComplete(0, new SQLException("List to be inserted cannot be empty!"));
        } else {
            executeUpdate(new OnGetPrepareStatement() {
                @Override
                public PreparedStatement onGetPreparedStatement(BackendConnection remoteConnection) {
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
                                onUpdateCompleteListener.onUpdateComplete(0, new SQLException("Parameter index was not incremented by the number of attributes during PreparedStatement preparation callback: " +
                                        "Expected " + entityAttributeNames.length + " got " + (newIndex - i)));
                                return null;
                            }
                            i += entityAttributeNames.length;
                        }
                        return preparedStatement;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        onUpdateCompleteListener.onUpdateComplete(0, e);
                        return null;
                    }
                }
            }, new OnUpdateCompleteListener() {
                @Override
                public void onUpdateComplete(int result, SQLException e) {
                    onUpdateCompleteListener.onUpdateComplete(result, e);
                }
            });
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
