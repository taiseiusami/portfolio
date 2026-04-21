# 在庫管理API

## 概要
在庫商品の登録・入出庫・履歴管理を行うREST APIです。

## 使用技術
- Java
- Spring Boot
- MySQL

## 機能
- 在庫登録（商品情報の登録）
- 入出庫管理（数量の増減処理）
- 在庫履歴管理（操作履歴の記録・参照）

## エンドポイント例
- POST /items（商品登録）
- GET /items（商品一覧取得）
- GET /items/{id}（商品詳細取得）
- PUT /items/{id}（商品更新）
- DELETE /items/{id}（商品削除）
- POST /stock/in（入庫処理）
- POST /stock/out（出庫処理）
- GET /stock/history（履歴取得）

## 工夫した点
- 三層構造（Controller / Service / Repository）で設計
- 楽観ロックによる排他制御を実装
- 入力値バリデーションを実装し、不正データを防止

## DB構成
- itemsテーブル（商品情報）
- stock_historyテーブル（在庫操作履歴）

## 動作環境
- Java 17
- Spring Boot
- MySQL

## 補足
学習目的で作成したプロジェクトです。
