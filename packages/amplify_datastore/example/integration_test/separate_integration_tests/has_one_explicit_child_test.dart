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

import 'package:integration_test/integration_test.dart';
import 'package:flutter_test/flutter_test.dart';

import '../utils/setup_utils.dart';
import '../utils/test_cloud_synced_model_operation.dart';
import 'models/has_one/ModelProvider.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  group('HasOne (parent refers to child with explicit connection field)', () {
    // schema
    // type HasOneParent @model {
    //   id: ID!
    //   name: String
    //   explicitChildID: ID
    //   explicitChild: HasOneChild @hasOne(fields: ["explicitChildID"])
    // }
    // type HasOneChild @model {
    //   id: ID!
    //   name: String
    // }
    final enableCloudSync = shouldEnableCloudSync();
    var associatedModels = [HasOneChild(name: 'child')];
    // Currently with @hasOne, parent -> child relationship is created
    // by assign child.id to the connection field of the parent
    var rootModels = [
      HasOneParent(
          name: 'HasOne (explicit)', explicitChildID: associatedModels.first.id)
    ];

    testRootAndAssociatedModelsRelationship(
      modelProvider: ModelProvider.instance,
      rootModelType: HasOneParent.classType,
      rootModels: rootModels,
      rootModelQueryIdentifier: HasOneParent.MODEL_IDENTIFIER,
      associatedModelType: HasOneChild.classType,
      associatedModels: associatedModels,
      associatedModelQueryIdentifier: HasOneChild.MODEL_IDENTIFIER,
      enableCloudSync: enableCloudSync,
    );
  });
}
