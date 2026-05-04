import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:timezone/timezone.dart' as tz;
import 'package:timezone/data/latest.dart' as tz_data;

/// 로컬 푸시 알림 서비스
class NotificationService {
  static final NotificationService _instance = NotificationService._internal();
  factory NotificationService() => _instance;
  NotificationService._internal();

  final FlutterLocalNotificationsPlugin _notifications = FlutterLocalNotificationsPlugin();
  bool _isInitialized = false;

  /// 알림 서비스 초기화
  Future<void> initialize() async {
    if (_isInitialized) return;

    // Timezone 데이터 초기화
    tz_data.initializeTimeZones();

    // Android 설정
    const androidSettings = AndroidInitializationSettings('@mipmap/ic_launcher');
    
    // iOS 설정
    const iosSettings = DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );

    const initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _notifications.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onNotificationResponse,
    );

    _isInitialized = true;
  }

  /// 알림 권한 요청
  Future<bool> requestPermissions() async {
    if (!_isInitialized) await initialize();

    // Android 13+ 권한 요청
    final androidImplementation = _notifications.resolvePlatformSpecificImplementation<AndroidFlutterLocalNotificationsPlugin>();
    if (androidImplementation != null) {
      final granted = await androidImplementation.requestNotificationsPermission();
      return granted ?? false;
    }

    // iOS 권한 요청
    final iosImplementation = _notifications.resolvePlatformSpecificImplementation<IOSFlutterLocalNotificationsPlugin>();
    if (iosImplementation != null) {
      final granted = await iosImplementation.requestPermissions(
        alert: true,
        badge: true,
        sound: true,
      );
      return granted ?? false;
    }

    return true;
  }

  /// 오늘의 말씀 알림 스케줄링
  Future<void> scheduleDailyVerseNotification({
    required String title,
    required String body,
    required TimeOfDay time,
  }) async {
    if (!_isInitialized) await initialize();

    // 기존 알림 취소
    await cancelDailyNotification();

    final now = DateTime.now();
    var scheduledDate = DateTime(
      now.year,
      now.month,
      now.day,
      time.hour,
      time.minute,
    );

    // 현재 시간이 이미 지났으면 내일로 설정
    if (scheduledDate.isBefore(now)) {
      scheduledDate = scheduledDate.add(const Duration(days: 1));
    }

    const androidDetails = AndroidNotificationDetails(
      'daily_verse_channel',
      '오늘의 말씀',
      channelDescription: '매일 정해진 시간에 말씀을 알려드립니다',
      importance: Importance.high,
      priority: Priority.high,
      showWhen: true,
      enableVibration: true,
      playSound: true,
      icon: '@mipmap/ic_launcher',
      largeIcon: DrawableResourceAndroidBitmap('@mipmap/ic_launcher'),
      color: Color(0xFF1A237E),
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    const notificationDetails = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _notifications.zonedSchedule(
      1, // 알림 ID
      title,
      body,
      tz.TZDateTime.from(scheduledDate, tz.local),
      notificationDetails,
      androidScheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
      uiLocalNotificationDateInterpretation: UILocalNotificationDateInterpretation.absoluteTime,
      matchDateTimeComponents: DateTimeComponents.time, // 매일 같은 시간에 반복
    );
  }

  /// 즉시 알림 표시 (테스트용)
  Future<void> showInstantNotification({
    required String title,
    required String body,
  }) async {
    if (!_isInitialized) await initialize();

    const androidDetails = AndroidNotificationDetails(
      'instant_channel',
      '테스트 알림',
      channelDescription: '즉시 표시되는 알림입니다',
      importance: Importance.high,
      priority: Priority.high,
      color: Color(0xFF1A237E),
    );

    const iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    const notificationDetails = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    await _notifications.show(
      Random().nextInt(10000),
      title,
      body,
      notificationDetails,
    );
  }

  /// 매일 알림 취소
  Future<void> cancelDailyNotification() async {
    await _notifications.cancel(1);
  }

  /// 모든 알림 취소
  Future<void> cancelAllNotifications() async {
    await _notifications.cancelAll();
  }

  /// 알림 응답 처리
  void _onNotificationResponse(NotificationResponse response) {
    // 알림 탭 시 앱 진입 처리
    debugPrint('Notification tapped: ${response.payload}');
  }

  /// 알림이 활성화되어 있는지 확인
  Future<bool> areNotificationsEnabled() async {
    if (!_isInitialized) await initialize();
    
    final pending = await _notifications.pendingNotificationRequests();
    return pending.any((n) => n.id == 1);
  }
}
