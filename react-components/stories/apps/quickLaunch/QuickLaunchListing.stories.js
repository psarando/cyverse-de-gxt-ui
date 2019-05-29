import React, { Component } from "react";
import QucikLaunchListing from "../../../src/apps/quickLaunch/QuickLaunchListing";

class QuickLaunchListingTest extends Component {
    render() {
        const onDelete = (qLaunch) => {
            console.log("Delete qlaunch: " + qLaunch.id);
        };

        const useQuickLaunch = (qLaunch) => {
            console.log("Use quick launch: " + qLaunch.id);
        };
        const onCreate = () => {
            console.log("Create quick launch");
        };
        const quickLaunches = [
            {
                id: "1",
                name: "Qlaunch1",
                description: "This is my first quick launch",
                creator: "sriram@iplantcollaborative.org",
                app_id: "456",
                is_public: false,
                submission: {
                    description: "string",
                    config: {},
                    "file-metadata": [
                        {
                            attr: "string",
                            value: "string",
                            unit: "string",
                        },
                    ],
                    starting_step: 0,
                    name: "string",
                    app_id: "string",
                    system_id: "string",
                    debug: true,
                    create_output_subdir: true,
                    archive_logs: true,
                    output_dir: "string",
                    uuid: "string",
                    notify: true,
                    "skip-parent-meta": true,
                    callback: "string",
                    job_id: "string",
                },
            },
            {
                id: "2",
                name: "Qlaunch2",
                description: "This is my second quick launch",
                creator: "ipctest@iplantcollaborative.org",
                app_id: "456",
                is_public: true,
                submission: {
                    description: "string",
                    config: {},
                    "file-metadata": [
                        {
                            attr: "string",
                            value: "string",
                            unit: "string",
                        },
                    ],
                    starting_step: 0,
                    name: "string",
                    app_id: "string",
                    system_id: "string",
                    debug: true,
                    create_output_subdir: true,
                    archive_logs: true,
                    output_dir: "string",
                    uuid: "string",
                    notify: true,
                    "skip-parent-meta": true,
                    callback: "string",
                    job_id: "string",
                },
            },
            {
                id: "3",
                name: "Qlaunch3",
                description: "This is my third quick launch",
                creator: "sriram@iplantcollaborative.org",
                app_id: "456",
                is_public: true,
                submission: {
                    description: "string",
                    config: {},
                    "file-metadata": [
                        {
                            attr: "string",
                            value: "string",
                            unit: "string",
                        },
                    ],
                    starting_step: 0,
                    name: "string",
                    app_id: "string",
                    system_id: "string",
                    debug: true,
                    create_output_subdir: true,
                    archive_logs: true,
                    output_dir: "string",
                    uuid: "string",
                    notify: true,
                    "skip-parent-meta": true,
                    callback: "string",
                    job_id: "string",
                },
            },
            {
                id: "4",
                name: "Qlaunch4",
                description: "This is my fourth quick launch",
                creator: "ipctest@iplantcollaborative.org",
                app_id: "456",
                is_public: false,
                submission: {
                    description: "string",
                    config: {},
                    "file-metadata": [
                        {
                            attr: "string",
                            value: "string",
                            unit: "string",
                        },
                    ],
                    starting_step: 0,
                    name: "string",
                    app_id: "string",
                    system_id: "string",
                    debug: true,
                    create_output_subdir: true,
                    archive_logs: true,
                    output_dir: "string",
                    uuid: "string",
                    notify: true,
                    "skip-parent-meta": true,
                    callback: "string",
                    job_id: "string",
                },
            },
        ];
        return (
            <QucikLaunchListing
                quickLaunches={quickLaunches}
                userName="sriram@iplantcollaborative.org"
                appId="456"
                systemId="de"
                onDelete={onDelete}
                useQuickLaunch={useQuickLaunch}
                onCreate={onCreate}
                baseDebugId="quicklaunchListing"
                loading={false}
                onSelection={() => console.log("Quick launch selected!")}
            />
        );
    }
}

export default QuickLaunchListingTest;
