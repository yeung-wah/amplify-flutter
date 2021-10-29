/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
*/

part of authenticator.form_field;

/// {@template authenticator.verify_user_form_field}
/// A [Radio] form field component on the Verify User screen.
///
/// Must be a descendant of a [VerifyUserFormFieldGroup] widget.
/// {@endtemplate}
class VerifyUserFormField<T>
    extends AuthenticatorFormField<T, T?, VerifyUserFormField<T>> {
  /// {@macro authenticator.verify_user_form_field}
  const VerifyUserFormField({
    Key? key,
    required T attributeKey,
    // InputResolverKey? labelKey, TODO
    String? label,
  }) : super._(
          key: key,
          field: attributeKey,
          // titleKey: labelKey,
          title: label,
        );

  @override
  _VerifyUserFormFieldState<T> createState() => _VerifyUserFormFieldState();
}

class _VerifyUserFormFieldState<T>
    extends _AuthenticatorFormFieldState<T, T?, VerifyUserFormField<T>> {
  ValueNotifier<T> get _groupValue => VerifyUserFormFieldGroup.of<T>(context);

  @override
  ValueChanged<T?> get onChanged {
    return (T? value) {
      if (value != null) {
        _groupValue.value = value;
      }
    };
  }

  @override
  Widget build(BuildContext context) {
    return ListTile(
      title: Text(widget.title!),
      leading: Radio<T>(
        value: widget.field,
        groupValue: _groupValue.value,
        onChanged: onChanged,
      ),
    );
  }
}

/// {@template authenticator.verify_user_form_field_group}
/// Wraps a group of [VerifyUserFormField] widgets ([Radio] buttons) to provide
/// common group state.
/// {@endtemplate}
class VerifyUserFormFieldGroup<T> extends InheritedNotifier {
  /// {@macro authenticator.verify_user_form_field_group}
  const VerifyUserFormFieldGroup({
    Key? key,
    required Widget child,
    required this.groupValue,
  }) : super(
          key: key,
          child: child,
          notifier: groupValue,
        );

  /// The value of the radio button group.
  final ValueNotifier<T> groupValue;

  static ValueNotifier<T> of<T>(BuildContext context) {
    final group = context
        .dependOnInheritedWidgetOfExactType<VerifyUserFormFieldGroup<T>>();
    assert(() {
      if (group == null) {
        throw FlutterError.fromParts([
          ErrorSummary('No VerifyUserFormFieldGroup widget found.'),
          ErrorDescription(
              'Make sure your VerifyUserFormFields are wrapped with an VerifyUserForm widget.')
        ]);
      }
      return true;
    }());
    return group!.groupValue;
  }

  @override
  void debugFillProperties(DiagnosticPropertiesBuilder properties) {
    super.debugFillProperties(properties);
    properties
        .add(DiagnosticsProperty<ValueNotifier<T>>('groupValue', groupValue));
  }
}

// ignore_for_file: prefer_asserts_with_message
