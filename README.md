# uvDataMask
日本語データマスキング向けライブラリです。

- 外部データに定義した設定値からマスク化することを想定しています。
- 生成する日本語はShift-JIS(MS932)の範囲としています。
- Java11+向けです。

## 乱数マスキング
| クラス | 概要 |
| --- | --- |
| RandomAddressGenerator | 住所をランダム生成します。※別途、選択元のデータが必要です。 |
| RandomCardnoReplacer | クレジットカード番号のランダム生成・置換を行います。 |
| RandomDataPickup | データのランダム選択を行います。 ※別途、選択元のデータが必要です。 |
| RandomDateGenerator | 日付をランダム生成します |
| RandomFullNameGenerator | 氏名をランダム生成します。 ※別途、選択元のデータが必要です。 |
| RandomListPickup | リストのランダム選択を行います。 |
| RandomMailAddrReplacer | メールアドレスをランダム置換します。 |
| RandomNumGenerator | 数値をランダム生成します。 |
| RandomTelnoReplacer | 電話番号をランダム生成します。 |
| RandomTextGenerator | 文字列をランダム生成します。 |
| RandomTextReplacer | 文字列をランダム置換します。 |
| ShuffleTextReplacer | 文字列をシャッフル置換します。 |

## 固定マスキング
| クラス | 概要 |
| --- | --- |
| FixedValueConverter | 固定値に置換します。 |
| MaskedTextReplacer | 文字列をパターンマスクします。 |
| NullToValue | Nullまたは空文字の場合に固定値に置換します。 |

## その他ETL向けユーティリティ
| クラス | 概要 |
| --- | --- |
| DateTimeConcat | 日付と時刻を日時に結合します。 |
| DateTimeSplit | 日時を日付と時刻に分割します。 |
| TextConcat | 複数文字列を1つに結合します。 |
| TextReplace | 文字列を置換します。 |
| TextSplit | 文字列を分割します。 |
| TextSubstr | 部分文字列に置換します。 |
| TextTrim | 前後の空白をTrimします。 |
| TypeConverter | 型変換します。 |
| DynamicExpression | 動的条件式(文字列入力)の判定を行います。 |
