package org.kawanfw.sql.api.client.android;

import java.sql.PreparedStatement;

public interface OnGetPreparedStatementListener {
    PreparedStatement onGetPreparedStatementListener(BackendConnection remoteConnection);
}
