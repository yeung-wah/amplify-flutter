import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';

import 'package:amplified_todo/main.dart' as app;

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('test', () {
    testWidgets('test',
            (WidgetTester tester) async {
      tester.printToConsole('before running app.main()');
          app.main();
        }, timeout:Timeout.none);
  });
}