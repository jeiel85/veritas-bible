import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/settings_provider.dart';

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
          _SectionHeader(title: '앱 정보'),
          const ListTile(
            title: Text('앱 버전'),
            trailing: Text('1.0.8+1'),
          ),
          const ListTile(
            title: Text('오픈소스 라이선스'),
            trailing: Icon(Icons.arrow_forward_ios, size: 14),
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
