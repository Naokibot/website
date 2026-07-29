# Java Web Defense Plugin

Jakarta Servlet対応Webアプリへ追加する、HTTP層のDDoS検知・緩和用フィルタープラグインです。先ほどの独立型リバースプロキシとは別プロジェクトです。

## 機能

- IPごとのトークンバケット型レート制限
- 短時間のバースト枠
- 長時間のローリング利用枠
- 利用枠超過後のクールダウン
- EWMAによる通常アクセス量の学習
- 急激なアクセス増加時の保護モード
- 違反を繰り返したIPの一時ブロック
- IPv4、IPv6、CIDRのホワイトリスト／ブラックリスト
- 信頼済みプロキシ経由の場合だけX-Forwarded-Forを使用
- JSON Lines形式の非同期ログ
- ログから攻撃傾向をまとめるMarkdownレポート

「Claude Freeのような制御」は非公開の内部仕様を再現したものではなく、バースト枠＋ローリング利用量＋クールダウンという一般的な制御です。

## ビルド

```bash
mvn clean test package
```

生成された`target/java-web-defense-plugin.jar`をWebアプリの`WEB-INF/lib/`へ追加します。Java 21以上、Jakarta Servlet 6対応環境が必要です。

外部設定を指定する例：

```bash
java -Dweb.defense.config=/absolute/path/web-defense.properties -jar your-app.jar
```

ログ分析：

```bash
java -jar java-web-defense-plugin.jar analyze logs/web-defense.jsonl reports/traffic.md
```

## 制限

このプラグインはJava Webアプリまで到達したHTTPアクセスを制御します。回線自体を埋める攻撃、DNS攻撃、JVMへ到達する前のTCP攻撃は止められません。公開サイトではCDN、WAF、ロードバランサーと併用してください。
