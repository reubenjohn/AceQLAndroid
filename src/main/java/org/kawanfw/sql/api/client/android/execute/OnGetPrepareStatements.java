package org.kawanfw.sql.api.client.android.execute;

import org.kawanfw.sql.api.client.android.BackendConnection;

import java.sql.PreparedStatement;

public interface OnGetPrepareStatements {
    PreparedStatement[] onGetPreparedStatements(BackendConnection remoteConnection);
}
