/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.gecko.telemetry.pingbuilders;

import org.mozilla.gecko.AppConstants;
import org.mozilla.gecko.sync.ExtendedJSONObject;
import org.mozilla.gecko.telemetry.TelemetryPing;

import java.util.Set;
import java.util.UUID;

/**
 * A generic Builder for {@link TelemetryPing} instances. Each overriding class is
 * expected to create a specific type of ping (e.g. "core").
 *
 * This base class handles the common ping operations under the hood:
 *   * Validating mandatory fields
 *   * Forming the server url
 */
abstract class TelemetryPingBuilder {
    // In the server url, the initial path directly after the "scheme://host:port/"
    private static final String SERVER_INITIAL_PATH = "submit/telemetry";

    private final String serverPath;
    protected final ExtendedJSONObject payload;
    private final int uniqueID;

    public TelemetryPingBuilder(final int uniqueID) {
        serverPath = getTelemetryServerPath(getDocType());
        payload = new ExtendedJSONObject();
        this.uniqueID = uniqueID;
    }

    /**
     * @return the name of the ping (e.g. "core")
     */
    public abstract String getDocType();

    /**
     * @return the fields that are mandatory for the resultant ping to be uploaded to
     *         the server. These will be validated before the ping is built.
     */
    public abstract String[] getMandatoryFields();

    public TelemetryPing build() {
        validatePayload();
        return new TelemetryPing(serverPath, payload, uniqueID);
    }

    private void validatePayload() {
        final Set<String> keySet = payload.keySet();
        for (final String mandatoryField : getMandatoryFields()) {
            if (!keySet.contains(mandatoryField)) {
                throw new IllegalArgumentException("Builder does not contain mandatory field: " +
                        mandatoryField);
            }
        }
    }

    /**
     * Returns a url of the format:
     *   http://hostname/submit/telemetry/docId/docType/appName/appVersion/appUpdateChannel/appBuildID
     *
     * @param docType The name of the ping (e.g. "main")
     * @return a url at which to POST the telemetry data to
     */
    private static String getTelemetryServerPath(final String docType) {
        final String docId = UUID.randomUUID().toString();
        final String appName = AppConstants.MOZ_APP_BASENAME;
        final String appVersion = AppConstants.MOZ_APP_VERSION;
        final String appUpdateChannel = AppConstants.MOZ_UPDATE_CHANNEL;
        final String appBuildId = AppConstants.MOZ_APP_BUILDID;

        // The compiler will optimize a single String concatenation into a StringBuilder statement.
        // If you change this `return`, be sure to keep it as a single statement to keep it optimized!
        return SERVER_INITIAL_PATH + '/' +
                docId + '/' +
                docType + '/' +
                appName + '/' +
                appVersion + '/' +
                appUpdateChannel + '/' +
                appBuildId;
    }
}
