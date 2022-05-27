/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.amplify.amplify_auth_cognito

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.annotation.UiThread
import aws.sdk.kotlin.runtime.auth.credentials.Credentials
import aws.smithy.kotlin.runtime.time.Instant
import com.amplifyframework.auth.AuthCodeDeliveryDetails
import com.amplifyframework.auth.AuthDevice
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthPlugin
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.cognito.AWSCognitoAuthServiceBehavior
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.cognito.AWSCognitoUserPoolTokens
import com.amplifyframework.auth.cognito.BuildConfig
import com.amplifyframework.auth.options.AuthConfirmResetPasswordOptions
import com.amplifyframework.auth.options.AuthConfirmSignInOptions
import com.amplifyframework.auth.options.AuthConfirmSignUpOptions
import com.amplifyframework.auth.options.AuthResendSignUpCodeOptions
import com.amplifyframework.auth.options.AuthResendUserAttributeConfirmationCodeOptions
import com.amplifyframework.auth.options.AuthResetPasswordOptions
import com.amplifyframework.auth.options.AuthSignInOptions
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.options.AuthUpdateUserAttributeOptions
import com.amplifyframework.auth.options.AuthUpdateUserAttributesOptions
import com.amplifyframework.auth.options.AuthWebUISignInOptions
import com.amplifyframework.auth.result.AuthResetPasswordResult
import com.amplifyframework.auth.result.AuthSessionResult
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.auth.result.AuthUpdateAttributeResult
import com.amplifyframework.core.Action
import com.amplifyframework.core.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject

class NativeAuthPlugin(
    private val nativeAuthPlugin: () -> NativeAuthPluginBindings.NativeAuthPlugin?
): AuthPlugin<AWSCognitoAuthServiceBehavior>() {

    override fun getPluginKey(): String = "awsCognitoAuthPlugin"

    override fun getEscapeHatch(): AWSCognitoAuthServiceBehavior? = null

    override fun getVersion(): String = BuildConfig.VERSION_NAME

    override fun configure(pluginConfiguration: JSONObject?, context: Context) {
        // No-op
    }

    override fun fetchAuthSession(
        onSuccess: Consumer<AuthSession>,
        onError: Consumer<AuthException>
    ) {
        val nativePlugin = nativeAuthPlugin()
        if (nativePlugin == null) {
            onError.accept(
                AuthException.UnknownException(
                    Exception("No native plugin registered")
                )
            )
            return
        }
        MainScope().launch {
            nativePlugin.fetchAuthSession(true) { session ->
                val couldNotFetchException = AuthException.UnknownException(
                    Exception("Could not fetch")
                )
                val userPoolTokens = if (session.userPoolTokens != null) {
                    val tokens = AWSCognitoUserPoolTokens(
                        session.userPoolTokens!!.accessToken,
                        session.userPoolTokens!!.idToken,
                        session.userPoolTokens!!.refreshToken,
                    )
                    AuthSessionResult.success(tokens)
                } else {
                    AuthSessionResult.failure(couldNotFetchException)
                }
                val awsCredentials = if (session.awsCredentials != null) {
                    val sessionCredentials = session.awsCredentials!!
                    val credentials = Credentials(
                        sessionCredentials.accessKeyId,
                        sessionCredentials.secretAccessKey,
                        sessionCredentials.sessionToken,
                        if (sessionCredentials.expirationIso8601Utc != null) Instant.fromIso8601(
                            sessionCredentials.expirationIso8601Utc!!
                        ) else null,
                    )
                    AuthSessionResult.success(credentials)
                } else {
                    AuthSessionResult.failure(couldNotFetchException)
                }
                val authSession = AWSCognitoAuthSession(
                    session.isSignedIn,
                    AuthSessionResult.success(session.identityId),
                    awsCredentials,
                    AuthSessionResult.success(session.userSub),
                    userPoolTokens,
                )
                onSuccess.accept(authSession)
            }
        }
    }

    override fun getCurrentUser(): AuthUser {
        fail("getCurrentUser is not supported")
    }

    override fun signUp(
        username: String,
        password: String,
        options: AuthSignUpOptions,
        onSuccess: Consumer<AuthSignUpResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signUp is not supported")
    }

    override fun confirmSignUp(
        username: String,
        confirmationCode: String,
        options: AuthConfirmSignUpOptions,
        onSuccess: Consumer<AuthSignUpResult>,
        onError: Consumer<AuthException>
    ) {
        fail("confirmSignUp is not supported")
    }

    override fun confirmSignUp(
        username: String,
        confirmationCode: String,
        onSuccess: Consumer<AuthSignUpResult>,
        onError: Consumer<AuthException>
    ) {
        fail("confirmSignUp is not supported")
    }

    override fun resendSignUpCode(
        username: String,
        options: AuthResendSignUpCodeOptions,
        onSuccess: Consumer<AuthSignUpResult>,
        onError: Consumer<AuthException>
    ) {
        fail("resendSignUpCode is not supported")
    }

    override fun resendSignUpCode(
        username: String,
        onSuccess: Consumer<AuthSignUpResult>,
        onError: Consumer<AuthException>
    ) {
        fail("resendSignUpCode is not supported")
    }

    override fun signIn(
        username: String?,
        password: String?,
        options: AuthSignInOptions,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signIn is not supported")
    }

    override fun signIn(
        username: String?,
        password: String?,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signIn is not supported")
    }

    override fun confirmSignIn(
        confirmationCode: String,
        options: AuthConfirmSignInOptions,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("confirmSignIn is not supported")
    }

    override fun confirmSignIn(
        confirmationCode: String,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("confirmSignIn is not supported")
    }

    override fun signInWithSocialWebUI(
        provider: AuthProvider,
        callingActivity: Activity,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signInWithSocialWebUI is not supported")
    }

    override fun signInWithSocialWebUI(
        provider: AuthProvider,
        callingActivity: Activity,
        options: AuthWebUISignInOptions,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signInWithSocialWebUI is not supported")
    }

    override fun signInWithWebUI(
        callingActivity: Activity,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signInWithWebUI is not supported")
    }

    override fun signInWithWebUI(
        callingActivity: Activity,
        options: AuthWebUISignInOptions,
        onSuccess: Consumer<AuthSignInResult>,
        onError: Consumer<AuthException>
    ) {
        fail("signInWithWebUI is not supported")
    }

    override fun handleWebUISignInResponse(intent: Intent?) {
        fail("handleWebUISignInResponse is not supported")
    }

    override fun rememberDevice(onSuccess: Action, onError: Consumer<AuthException>) {
        fail("rememberDevice is not supported")
    }

    override fun forgetDevice(onSuccess: Action, onError: Consumer<AuthException>) {
        fail("forgetDevice is not supported")
    }

    override fun forgetDevice(
        device: AuthDevice,
        onSuccess: Action,
        onError: Consumer<AuthException>
    ) {
        fail("forgetDevice is not supported")
    }

    override fun fetchDevices(
        onSuccess: Consumer<MutableList<AuthDevice>>,
        onError: Consumer<AuthException>
    ) {
        fail("fetchDevices is not supported")
    }

    override fun resetPassword(
        username: String,
        options: AuthResetPasswordOptions,
        onSuccess: Consumer<AuthResetPasswordResult>,
        onError: Consumer<AuthException>
    ) {
        fail("resetPassword is not supported")
    }

    override fun resetPassword(
        username: String,
        onSuccess: Consumer<AuthResetPasswordResult>,
        onError: Consumer<AuthException>
    ) {
        fail("resetPassword is not supported")
    }

    override fun confirmResetPassword(
        newPassword: String,
        confirmationCode: String,
        options: AuthConfirmResetPasswordOptions,
        onSuccess: Action,
        onError: Consumer<AuthException>
    ) {
        fail("confirmResetPassword is not supported")
    }

    override fun confirmResetPassword(
        newPassword: String,
        confirmationCode: String,
        onSuccess: Action,
        onError: Consumer<AuthException>
    ) {
        fail("confirmResetPassword is not supported")
    }

    override fun updatePassword(
        oldPassword: String,
        newPassword: String,
        onSuccess: Action,
        onError: Consumer<AuthException>
    ) {
        fail("updatePassword is not supported")
    }

    override fun fetchUserAttributes(
        onSuccess: Consumer<MutableList<AuthUserAttribute>>,
        onError: Consumer<AuthException>
    ) {
        fail("fetchUserAttributes is not supported")
    }

    override fun updateUserAttribute(
        attribute: AuthUserAttribute,
        options: AuthUpdateUserAttributeOptions,
        onSuccess: Consumer<AuthUpdateAttributeResult>,
        onError: Consumer<AuthException>
    ) {
        fail("updateUserAttribute is not supported")
    }

    override fun updateUserAttribute(
        attribute: AuthUserAttribute,
        onSuccess: Consumer<AuthUpdateAttributeResult>,
        onError: Consumer<AuthException>
    ) {
        fail("updateUserAttribute is not supported")
    }

    override fun updateUserAttributes(
        attributes: MutableList<AuthUserAttribute>,
        options: AuthUpdateUserAttributesOptions,
        onSuccess: Consumer<MutableMap<AuthUserAttributeKey, AuthUpdateAttributeResult>>,
        onError: Consumer<AuthException>
    ) {
        fail("updateUserAttributes is not supported")
    }

    override fun updateUserAttributes(
        attributes: MutableList<AuthUserAttribute>,
        onSuccess: Consumer<MutableMap<AuthUserAttributeKey, AuthUpdateAttributeResult>>,
        onError: Consumer<AuthException>
    ) {
        fail("updateUserAttributes is not supported")
    }

    override fun resendUserAttributeConfirmationCode(
        attributeKey: AuthUserAttributeKey,
        options: AuthResendUserAttributeConfirmationCodeOptions,
        onSuccess: Consumer<AuthCodeDeliveryDetails>,
        onError: Consumer<AuthException>
    ) {
        fail("resendUserAttributeConfirmationCode is not supported")
    }

    override fun resendUserAttributeConfirmationCode(
        attributeKey: AuthUserAttributeKey,
        onSuccess: Consumer<AuthCodeDeliveryDetails>,
        onError: Consumer<AuthException>
    ) {
        fail("resendUserAttributeConfirmationCode is not supported")
    }

    override fun confirmUserAttribute(
        attributeKey: AuthUserAttributeKey,
        confirmationCode: String,
        onSuccess: Action,
        onError: Consumer<AuthException>
    ) {
        fail("confirmUserAttribute is not supported")
    }

    override fun signOut(onSuccess: Action, onError: Consumer<AuthException>) {
        fail("signOut is not supported")
    }

    override fun signOut(
        options: AuthSignOutOptions,
        onSuccess: Action,
        onError: Consumer<AuthException>
    ) {
        fail("signOut is not supported")
    }

    override fun deleteUser(onSuccess: Action, onError: Consumer<AuthException>) {
        fail("deleteUser is not supported")
    }

    private fun fail(message: String): Nothing {
        throw IllegalStateException(message)
    }

}
