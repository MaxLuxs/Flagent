import * as vscode from "vscode";
import { FlagsTreeProvider, FlagTreeItem } from "./tree/FlagsTreeProvider";

export function activate(context: vscode.ExtensionContext): void {
  const provider = new FlagsTreeProvider();

  context.subscriptions.push(
    vscode.window.registerTreeDataProvider("flagentFlags", provider)
  );

  context.subscriptions.push(
    vscode.commands.registerCommand("flagent.refresh", () => provider.refresh())
  );

  context.subscriptions.push(
    vscode.commands.registerCommand("flagent.copyKey", (item: FlagTreeItem) => {
      provider.copyKey(item.flag);
    })
  );

  context.subscriptions.push(
    vscode.commands.registerCommand("flagent.insertSnippet", (item: FlagTreeItem) => {
      provider.insertSnippet(item.flag);
    })
  );

  context.subscriptions.push(
    vscode.commands.registerCommand("flagent.configure", () => {
      vscode.commands.executeCommand("workbench.action.openSettings", "flagent");
    })
  );
}

export function deactivate(): void {}
