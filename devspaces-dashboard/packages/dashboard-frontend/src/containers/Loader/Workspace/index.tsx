/*
 * Copyright (c) 2018-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

import React from 'react';
import { connect, ConnectedProps } from 'react-redux';
import { AppState } from '../../../store';
import { selectAllWorkspaces, selectLogs } from '../../../store/Workspaces/selectors';
import * as WorkspaceStore from '../../../store/Workspaces';
import { List, LoaderStep, LoadingStep } from '../../../components/Loader/Step';
import StepInitialize from './Steps/Initialize';
import StepStartWorkspace from './Steps/StartWorkspace';
import StepOpenWorkspace from './Steps/OpenWorkspace';
import findTargetWorkspace from './findTargetWorkspace';

export type Props = MappedProps & {
  currentStepIndex: number; // not ID, but index
  loaderSteps: Readonly<List<LoaderStep>>;
  matchParams: {
    namespace: string;
    workspaceName: string;
  };
  tabParam: string | undefined;
  onNextStep: () => void;
  onRestart: () => void;
};

class WorkspaceLoader extends React.PureComponent<Props> {
  constructor(props: Props) {
    super(props);
  }

  private handleWorkspaceRestart(): void {
    const { allWorkspaces, matchParams } = this.props;
    const workspace = findTargetWorkspace(allWorkspaces, matchParams);
    if (workspace) {
      this.props.deleteWorkspaceLogs(workspace);
    }

    this.props.onRestart();
  }

  render(): React.ReactNode {
    const { currentStepIndex, loaderSteps } = this.props;

    switch (loaderSteps.get(currentStepIndex).value.id) {
      case LoadingStep.INITIALIZE:
        return <StepInitialize {...this.props} onRestart={() => this.handleWorkspaceRestart()} />;
      case LoadingStep.START_WORKSPACE:
        return (
          <StepStartWorkspace {...this.props} onRestart={() => this.handleWorkspaceRestart()} />
        );
      default:
        return (
          <StepOpenWorkspace {...this.props} onRestart={() => this.handleWorkspaceRestart()} />
        );
    }
  }
}

const mapStateToProps = (state: AppState) => ({
  allWorkspaces: selectAllWorkspaces(state),
  workspacesLogs: selectLogs(state),
});

const connector = connect(mapStateToProps, WorkspaceStore.actionCreators);
type MappedProps = ConnectedProps<typeof connector>;
export default connector(WorkspaceLoader);
