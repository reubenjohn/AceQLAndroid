package org.kawanfw.sql.api.client.android;

import java.sql.SQLException;

public interface OnRemoteConnectionEstablishedListener {

    void onRemoteConnectionEstablishedListener(BackendConnection remoteConnection, SQLException e);
}
