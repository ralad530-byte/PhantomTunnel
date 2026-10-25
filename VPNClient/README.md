# Shadow VPN — تطبيق عميل V2Ray/Xray احترافي لأندرويد

تطبيق Android كامل (Kotlin + Jetpack Compose) بنفس فلسفة v2rayNG / V2Box، يدعم
استيراد السيرفرات عبر روابط المشاركة أو الاشتراكات، مع واجهة احترافية داكنة
وقياس زمن استجابة وإحصاء حركة البيانات.

## ما تم بناؤه (جاهز وفعلي)
- هيكل مشروع Android Studio كامل (Gradle Kotlin DSL).
- محلل روابط (`UriParser`) لكل الصيغ: `vmess://` `vless://` `trojan://` `ss://` `hysteria2://` `tuic://`.
- مولّد إعدادات (`ConfigBuilder`) يحوّل أي سيرفر إلى JSON متوافق مع Xray-core
  (vmess/vless/trojan/shadowsocks عبر xray-core القياسي، hysteria2/tuic بصيغتها الخاصة).
- `VpnConnectionService` فعلية تفتح واجهة TUN حقيقية عبر `android.net.VpnService`،
  مع توجيه استثناء الشبكة المحلية، ودعم استبعاد تطبيقات معيّنة من النفق (Per-App Proxy).
- قاعدة بيانات Room كاملة (سيرفرات + اشتراكات)، مع تحديث اشتراك عبر HTTP (OkHttp).
- قياس Ping حقيقي (TCP connect latency) لكل سيرفر.
- واجهة Compose كاملة: قائمة سيرفرات، زر اتصال دائري، إضافة سيرفر بالرابط،
  شاشة اشتراكات، شاشة إعدادات (LAN bypass, per-app proxy toggle, auto-connect).
- دعم فتح الروابط مباشرة من المتصفح (`vless://...` إلخ) عبر Deep Link.

## الجزء الوحيد غير المضمّن: محرك Xray الأصلي (Go binary)
كل تطبيقات V2Ray/Xray المفتوحة المصدر — v2rayNG نفسه، V2Box، NekoBox — **لا تكتب
بروتوكولات VMess/VLESS/Trojan بلغة Kotlin/Java**. هي تستدعي مكتبة Go مُصرّفة إلى
Android عبر `gomobile`. هذا الجزء يتطلب بيئة Go + Android NDK لبنائه، وهو غير
متاح في بيئة التنفيذ الحالية (بدون إنترنت/SDK).

خطوات الدمج (موثّقة ومعتمدة من مجتمع v2rayNG نفسه):

```bash
# 1. ثبّت Go + gomobile
go install golang.org/x/mobile/cmd/gomobile@latest
gomobile init

# 2. استنسخ المحرك الجاهز (نفس ما يستخدمه v2rayNG)
git clone https://github.com/2dust/AndroidLibXrayLite
cd AndroidLibXrayLite

# 3. ابنِ ملف aar
gomobile bind -target=android -androidapi 24 -o libv2ray.aar ./

# 4. انسخ الناتج إلى المشروع
cp libv2ray.aar /path/to/ShadowVPN/app/libs/
```

ثم في `app/build.gradle.kts` فعّل السطر:
```kotlin
implementation(files("libs/libv2ray.aar"))
```

وفي `app/src/main/java/com/shadow/vpnclient/core/XrayCoreManager.kt` استبدل
التعليقات `// TODO` باستدعاءات `Libv2ray.newV2RayPoint(...)` الفعلية — الكلاس
مصمم خصيصاً بحيث هذا الملف هو **الوحيد** الذي يحتاج تعديل لربط المحرك الحقيقي؛
باقي التطبيق (الواجهة، القاعدة، الخدمة) لا يلمس الـ API الأصلي إطلاقاً.

لـ Hysteria2/TUIC تحديداً: xray-core الرسمي لا يدعمها كمخرج outbound مباشر؛
الحل المعتمد في V2Box هو تضمين ثنائيين إضافيين (`hysteria` و `tuic-client`)
أو استخدام `sing-box` كمحرك بديل يدعم كل البروتوكولات الستة في Go واحد
(https://github.com/SagerNet/sing-box) — وهو الخيار الأبسط إن كنت تريد
توحيد المحرك بدلاً من تشغيل عدة ثنائيات.

## البناء
افتح المجلد في Android Studio (Hedgehog أو أحدث)، دعه يزامن Gradle، ثم Run.
الحد الأدنى لإصدار أندرويد: 7.0 (API 24).

## بنية المشروع
```
app/src/main/java/com/shadow/vpnclient/
  core/      VpnConnectionService, XrayCoreManager, ConfigBuilder
  model/     Protocol, ServerConfig
  parser/    UriParser (كل صيغ الروابط)
  data/      Room: ServerEntity/Dao, SubscriptionRepository
  ui/        MainViewModel + شاشات Compose
  util/      PingTester, TrafficStatsMonitor
```

## ملاحظة قانونية
استخدام VPN مشروع في أغلب الدول لأغراض الخصوصية والأمان على الشبكات العامة،
لكن قوانين بعض الدول تقيّد تجاوز الرقابة تحديداً. تأكد من الالتزام بقوانين بلدك
وبشروط استخدام أي سيرفر تتصل به.
