package org.kawanfw.sql.api.client.android;

import android.os.AsyncTask;

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

    public static void getDefaultRemoteConnectionIfExists(OnRemoteConnectionEstablishedListener listener) {
        getRemoteConnection(backendUrl, listener);
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

    public static void executePreparedStatements(final OnPrepareStatements onPrepareStatements, final OnGetResultSetListener onGetResultSetListener) {
        AceQLDBManager.getDefaultRemoteConnectionIfExists(new OnRemoteConnectionEstablishedListener() {
            @Override
            public void onRemoteConnectionEstablishedListener(final BackendConnection remoteConnection, SQLException e) {
                if (e != null) {
                    onGetResultSetListener.onGetResultSets(null, e);
                } else {
                    PreparedStatement[] preparedStatements = onPrepareStatements.onGetPreparedStatementListener(remoteConnection);
                    if (preparedStatements != null) {
                        ExecutePreparedStatementTask executePreparedStatementTask = new ExecutePreparedStatementTask();
                        executePreparedStatementTask.setOnGetResultListener(onGetResultSetListener);
                        executePreparedStatementTask.execute(preparedStatements);
                    } else {
                        onGetResultSetListener.onGetResultSets(null, new SQLException("Null prepared statement"));
                    }
                }
            }
        });
    }

    public static <T> void getSelectedLists(final String selectStatement, final ItemBuilder<T> itemBuilder, final OnQueryComplete<T> onQueryComplete) {
        executePreparedStatements(new OnPrepareStatements() {
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
            public void onGetResultSets(ResultSet[] resultSets, SQLException e) {
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

    public static <T> void performRawInsertion(final String sql, final List<T> list,
                                               final OnInsertListener<T> onInsertListener) {
        executePreparedStatements(new OnPrepareStatements() {
            @Override
            public PreparedStatement[] onGetPreparedStatementListener(BackendConnection remoteConnection) {
                PreparedStatement[] preparedStatements = new PreparedStatement[list.size()];

                try {
                    for (int i = 0; i < preparedStatements.length; i++) {
                        preparedStatements[i] = remoteConnection.getConnection().prepareStatement(sql);
                        onInsertListener.onInsertRow(preparedStatements[i], list.get(i));
                    }
                    return preparedStatements;
                } catch (SQLException e1) {
                    onInsertListener.onResult(e1);
                    return null;
                }
            }
        }, new OnGetResultSetListener() {
            @Override
            public void onGetResultSets(ResultSet[] resultSets, SQLException e) {
                onInsertListener.onResult(e);
            }
        });
    }

    public static <T extends SQLEntity> void performDefaultInsert(List<T> list, OnQueryComplete<T> onQueryComplete) {
        if (list.isEmpty()) {
            onQueryComplete.onQueryComplete(null, new SQLException("List to be inserted cannot be empty!"));
        } else {
            SQLEntity entity = list.get(0);
            String entityName = entity.getEntityName();
            StringBuilder statementBuilder = new StringBuilder("insert into " + entityName + " (");
            for (String attribute : entity.getAttributeNames()) {
                statementBuilder.append(attribute);
                statementBuilder.append(", ");
            }
            statementBuilder.deleteCharAt(statementBuilder.length() - 1);
            statementBuilder.deleteCharAt(statementBuilder.length() - 1);
            statementBuilder.append(") ");
//                    "values(?, ?, ?)";
            executePreparedStatements(new OnPrepareStatements() {
                @Override
                public PreparedStatement[] onGetPreparedStatementListener(BackendConnection remoteConnection) {
                    return new PreparedStatement[0];
                }
            }, new OnGetResultSetListener() {
                @Override
                public void onGetResultSets(ResultSet[] rs, SQLException e) {

                }
            });
        }
    }

    public static String getServerUrl() {
        return backendUrl;
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
