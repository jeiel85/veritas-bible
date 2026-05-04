import 'dart:convert';
import 'dart:math';
import 'dart:typed_data';
import 'package:encrypt/encrypt.dart' as encrypt;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

/// 보안 저장소 서비스
/// 민감한 데이터의 안전한 저장 및 암호화를 담당
class SecureStorageService {
  static final SecureStorageService _instance = SecureStorageService._internal();
  factory SecureStorageService() => _instance;
  SecureStorageService._internal();

  final _secureStorage = const FlutterSecureStorage(
    aOptions: AndroidOptions(
      encryptedSharedPreferences: true,
      keyCipherAlgorithm: KeyCipherAlgorithm.RSA_ECB_PKCS1Padding,
      storageCipherAlgorithm: StorageCipherAlgorithm.AES_GCM_NoPadding,
    ),
    iOptions: IOSOptions(
      accountName: 'flutter_secure_storage_service',
      accessibility: KeychainAccessibility.first_unlock_this_device,
    ),
  );

  // 암호화 키 저장 키
  static const String _encryptionKeyName = 'app_encryption_key';
  
  // 암호화 인스턴스 캐시
  encrypt.Encrypter? _encrypter;
  encrypt.IV? _iv;

  /// 초기화 및 암호화 키 설정
  Future<void> initialize() async {
    await _initializeEncryptionKey();
  }

  /// 암호화 키 초기화 또는 생성
  Future<void> _initializeEncryptionKey() async {
    String? keyString = await _secureStorage.read(key: _encryptionKeyName);
    
    if (keyString == null) {
      // 새로운 키 생성
      final key = encrypt.Key.fromSecureRandom(32);
      keyString = base64Encode(key.bytes);
      await _secureStorage.write(key: _encryptionKeyName, value: keyString);
    }

    final keyBytes = base64Decode(keyString);
    final key = encrypt.Key(Uint8List.fromList(keyBytes));
    
    _encrypter = encrypt.Encrypter(
      encrypt.AES(key, mode: encrypt.AESMode.cbc, padding: 'PKCS7'),
    );
    
    // 고정 IV 사용 (실제 프로덕션에서는 매번 다른 IV 사용 권장)
    _iv = encrypt.IV.fromLength(16);
  }

  /// 안전한 저장소에 값 쓰기
  Future<void> write(String key, String value) async {
    await _secureStorage.write(key: key, value: value);
  }

  /// 안전한 저장소에서 값 읽기
  Future<String?> read(String key) async {
    return await _secureStorage.read(key: key);
  }

  /// 안전한 저장소에서 값 삭제
  Future<void> delete(String key) async {
    await _secureStorage.delete(key: key);
  }

  /// 모든 보안 데이터 삭제
  Future<void> deleteAll() async {
    await _secureStorage.deleteAll();
  }

  /// 텍스트 암호화
  String encryptText(String plainText) {
    if (_encrypter == null || _iv == null) {
      throw Exception('암호화 서비스가 초기화되지 않았습니다');
    }
    
    final encrypted = _encrypter!.encrypt(plainText, iv: _iv);
    return encrypted.base64;
  }

  /// 텍스트 복호화
  String decryptText(String encryptedText) {
    if (_encrypter == null || _iv == null) {
      throw Exception('암호화 서비스가 초기화되지 않았습니다');
    }
    
    try {
      final encrypted = encrypt.Encrypted.fromBase64(encryptedText);
      return _encrypter!.decrypt(encrypted, iv: _iv);
    } catch (e) {
      // 복호화 실패 시 원본 반환 (마이그레이션 용이성)
      return encryptedText;
    }
  }

  /// 민감한 데이터가 암호화되어 있는지 확인
  bool isEncrypted(String text) {
    if (text.isEmpty) return false;
    
    try {
      // Base64 디코딩 시도
      final decoded = base64Decode(text);
      // 암호화된 데이터는 일반적으로 16바이트 이상의 길이를 가짐
      return decoded.length >= 16;
    } catch (e) {
      return false;
    }
  }
}

/// 보안 키 상수
class SecureStorageKeys {
  static const String userEncryptionKey = 'user_encryption_key';
  static const String appInitialized = 'app_initialized_secure';
  static const String userNotesKey = 'user_notes_encrypted';
}
