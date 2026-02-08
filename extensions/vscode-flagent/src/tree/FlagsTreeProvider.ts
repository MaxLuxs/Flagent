import type { Flag } from "@flagent/client";
import * as vscode from "vscode";
import { createFlagApi } from "../api/createFlagApi";

export class FlagTreeItem extends vscode.TreeItem {
  constructor(
    public readonly flag: Flag,
    collapsibleState: vscode.TreeItemCollapsibleState
  ) {
    super(flag.key, collapsibleState);
    this.tooltip = flag.description || flag.key;
    this.description = flag.enabled ? "on" : "off";
    this.contextValue = "flag";
  }
}

export class FlagsTreeProvider implements vscode.TreeDataProvider<FlagTreeItem> {
  private _onDidChangeTreeData = new vscode.EventEmitter<FlagTreeItem | undefined>();
  readonly onDidChangeTreeData = this._onDidChangeTreeData.event;

  private getApi() {
    const config = vscode.workspace.getConfiguration("flagent");
    const baseUrl = config.get<string>("baseUrl") ?? "http://localhost:18000";
    const apiKey = config.get<string>("apiKey");
    if (!baseUrl) return null;
    return createFlagApi(baseUrl, apiKey || undefined);
  }

  refresh(): void {
    this._onDidChangeTreeData.fire(undefined);
  }

  getTreeItem(element: FlagTreeItem): vscode.TreeItem {
    return element;
  }

  async getChildren(): Promise<FlagTreeItem[]> {
    const api = this.getApi();
    if (!api) return [];
    try {
      const { data: flags } = await api.findFlags(100);
      return (flags ?? []).map((f) => new FlagTreeItem(f, vscode.TreeItemCollapsibleState.None));
    } catch (e) {
      const msg = e instanceof Error ? e.message : String(e);
      vscode.window.showErrorMessage(`Flagent: ${msg}`);
      return [];
    }
  }

  async copyKey(flag: Flag): Promise<void> {
    await vscode.env.clipboard.writeText(flag.key);
    vscode.window.showInformationMessage(`Copied: ${flag.key}`);
  }

  async insertSnippet(flag: Flag): Promise<void> {
    const editor = vscode.window.activeTextEditor;
    if (!editor) {
      vscode.window.showWarningMessage("No active editor");
      return;
    }
    const snippet = `"${flag.key}"`;
    await editor.insertSnippet(new vscode.SnippetString(snippet));
  }
}
