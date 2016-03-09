package org.kawanfw.sql.api.client.android;

import java.sql.PreparedStatement;

public interface OnPrepareStatements {
    PreparedStatement[] onGetPreparedStatementListener(BackendConnection remoteConnection);
}
