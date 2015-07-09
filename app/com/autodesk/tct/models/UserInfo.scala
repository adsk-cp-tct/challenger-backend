package com.autodesk.tct.models

import java.util.UUID

/**
 * Sign up user
 *
 * @param nickName
 * @param email
 * @param password
 */
case class SignUpUser(nickName: String,
                      email: String,
                      password: String)

/**
 * Sign in user
 *
 * @param id
 * @param avatar
 * @param nickName
 * @param realName
 * @param email
 */
case class SignInUser(id: UUID,
                      avatar: String,
                      nickName: String,
                      realName: String,
                      email: String)
