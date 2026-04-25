import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/settings_provider.dart';
import '../providers/bible_provider.dart';
import 'setup_screen.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final settings = Provider.of<SettingsProvider>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('설정'),
      ),
      body: ListView(
        children: [
          _SectionHeader(title: '일반 설정'),
          SwitchListTile(
            title: const Text('다크 모드'),
            subtitle: const Text('야간 읽기에 적합한 어두운 테마'),
            value: settings.isDarkMode,
            onChanged: (val) => settings.toggleTheme(),
          ),
          ListTile(
            title: const Text('테마 색상'),
            subtitle: const Text('앱의 주요 강조 색상을 변경합니다.'),
            trailing: CircleAvatar(backgroundColor: settings.themeColor, radius: 15),
            onTap: () => _showColorPicker(context, settings),
          ),
          const Divider(),
          _SectionHeader(title: '본문 가독성'),
          ListTile(
            title: const Text('폰트 스타일'),
            subtitle: Text(settings.fontFamily == 'Serif' ? '명조체 (장문 읽기 권장)' : '고딕체 (깔끔한 시스템 서체)'),
            trailing: SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'Sans-serif', label: Text('고딕'), icon: Icon(Icons.font_download_outlined)),
                ButtonSegment(value: 'Serif', label: Text('명조'), icon: Icon(Icons.font_download)),
              ],
              selected: {settings.fontFamily},
              onSelectionChanged: (Set<String> newSelection) {
                settings.updateFontFamily(newSelection.first);
              },
            ),
          ),
          ListTile(
            title: const Text('폰트 크기'),
            subtitle: Slider(
              value: settings.fontSize,
              min: 10,
              max: 40,
              divisions: 30,
              label: settings.fontSize.round().toString(),
              onChanged: (val) => settings.updateFontSize(val),
            ),
            trailing: Text('${settings.fontSize.round()}pt'),
          ),
          ListTile(
            title: const Text('줄 간격 (행간)'),
            subtitle: Slider(
              value: settings.lineHeight,
              min: 1.0,
              max: 3.0,
              divisions: 20,
              label: settings.lineHeight.toStringAsFixed(1),
              onChanged: (val) => settings.updateLineHeight(val),
            ),
            trailing: Text('${settings.lineHeight.toStringAsFixed(1)}x'),
          ),
          const Divider(),
          _SectionHeader(title: '알림 설정 (스마트 리마인더)'),
          SwitchListTile(
            title: const Text('매일 말씀 알림'),
            subtitle: const Text('설정한 시간에 오늘의 말씀을 알림으로 보내드립니다.'),
            value: settings.isNotificationEnabled,
            onChanged: (val) => settings.toggleNotification(val),
          ),
          if (settings.isNotificationEnabled)
            ListTile(
              title: const Text('알림 시간 설정'),
              subtitle: Text('현재 설정: ${settings.notificationTime}'),
              trailing: const Icon(Icons.access_time),
              onTap: () async {
                final TimeOfDay? picked = await showTimePicker(
                  context: context,
                  initialTime: TimeOfDay(
                    hour: int.parse(settings.notificationTime.split(':')[0]),
                    minute: int.parse(settings.notificationTime.split(':')[1]),
                  ),
                );
                if (picked != null) {
                  final String formattedTime = 
                      '${picked.hour.toString().padLeft(2, '0')}:${picked.minute.toString().padLeft(2, '0')}';
                  settings.updateNotificationTime(formattedTime);
                }
              },
            ),
          const Divider(),
          _SectionHeader(title: '오디오 (TTS)'),
          ListTile(
            title: const Text('읽기 속도'),
            subtitle: Slider(
              value: settings.ttsSpeed,
              min: 0.1,
              max: 2.0,
              divisions: 19,
              label: '${settings.ttsSpeed}x',
              onChanged: (val) => settings.updateTtsSpeed(val),
            ),
            trailing: Text('${settings.ttsSpeed}x'),
          ),
          const Divider(),
          _SectionHeader(title: '데이터 관리'),
          ListTile(
            leading: const Icon(Icons.cloud_download_outlined, color: Colors.orange),
            title: const Text('성경 데이터 재설치'),
            subtitle: const Text('본문 데이터가 유실되었거나 최신화가 필요한 경우 실행하세요.'),
            onTap: () => _showResetDialog(context),
          ),
          const Divider(),
          _SectionHeader(title: '계정 및 동기화 (Issue #42, #43)'),
          ListTile(
            leading: const Icon(Icons.sync),
            title: const Text('멀티 디바이스 동기화'),
            subtitle: const Text('계정을 연동하여 여러 기기에서 데이터를 공유합니다.'),
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('구글 로그인 기능이 준비 중입니다.'))
              );
            },
          ),
          ListTile(
            leading: const Icon(Icons.cloud_upload),
            title: const Text('클라우드 백업 및 복원'),
            subtitle: const Text('데이터를 Google Drive / iCloud에 안전하게 백업합니다.'),
            onTap: () {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('클라우드 백업 기능을 구성 중입니다.'))
              );
            },
          ),
          const Divider(),
          _SectionHeader(title: '다운로드 및 오프라인 (Issue #44)'),
          ListTile(
            leading: const Icon(Icons.download_for_offline),
            title: const Text('오프라인 전체 모드 설정'),
            subtitle: const Text('모든 성경 데이터 및 미디어 자원을 다운로드합니다.'),
            onTap: () {
              showDialog(
                context: context,
                builder: (context) => AlertDialog(
                  title: const Text('리소스 다운로드'),
                  content: const Text('성경 텍스트, 오디오, 지도 리소스를 모두 다운로드하시겠습니까? (약 150MB)'),
                  actions: [
                    TextButton(onPressed: () => Navigator.pop(context), child: const Text('취소')),
                    ElevatedButton(onPressed: () => Navigator.pop(context), child: const Text('다운로드 시작')),
                  ],
                ),
              );
            },
          ),
          const Divider(),
          _SectionHeader(title: '앱 정보'),
          const ListTile(
            title: Text('앱 버전'),
            trailing: Text('1.3.0+1'),
          ),
          const ListTile(
            title: Text('오픈소스 라이선스'),
            trailing: Icon(Icons.arrow_forward_ios, size: 14),
          ),
        ],
      ),
    );
  }

  void _showResetDialog(BuildContext context) {
    final settings = Provider.of<SettingsProvider>(context, listen: false);
    final bible = Provider.of<BibleProvider>(context, listen: false);

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('성경 데이터 재설치'),
        content: const Text('현재 저장된 모든 성경 본문을 삭제하고 최신 데이터를 다시 다운로드하시겠습니까?\n\n(작성하신 북마크, 메모 등은 유지됩니다.)'),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text('취소')),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
            ),
            onPressed: () async {
              await bible.clearAllData();
              await settings.setInitialized(false);
              if (context.mounted) {
                Navigator.of(context).pushAndRemoveUntil(
                  MaterialPageRoute(builder: (_) => const SetupScreen()),
                  (route) => false,
                );
              }
            },
            child: const Text('재설치 시작'),
          ),
        ],
      ),
    );
  }

  void _showColorPicker(BuildContext context, SettingsProvider settings) {
    final List<Color> themeColors = [
      const Color(0xFF1A237E), // Navy
      const Color(0xFF4E342E), // Brown
      const Color(0xFF00695C), // Teal
      const Color(0xFF37474F), // Blue Grey
      const Color(0xFF212121), // Black
    ];

    showModalBottomSheet(
      context: context,
      builder: (context) {
        return Container(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('테마 색상 선택', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
              const SizedBox(height: 20),
              SizedBox(
                height: 60,
                child: ListView.builder(
                  scrollDirection: Axis.horizontal,
                  itemCount: themeColors.length,
                  itemBuilder: (context, index) {
                    return GestureDetector(
                      onTap: () {
                        settings.updateThemeColor(themeColors[index]);
                        Navigator.pop(context);
                      },
                      child: Container(
                        width: 50,
                        margin: const EdgeInsets.only(right: 15),
                        decoration: BoxDecoration(
                          color: themeColors[index],
                          shape: BoxShape.circle,
                          border: Border.all(
                            color: settings.themeColor == themeColors[index] ? Colors.blue : Colors.transparent,
                            width: 3,
                          ),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String title;
  const _SectionHeader({required this.title});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title,
        style: TextStyle(
          color: Theme.of(context).colorScheme.primary,
          fontWeight: FontWeight.bold,
          fontSize: 14,
        ),
      ),
    );
  }
}
