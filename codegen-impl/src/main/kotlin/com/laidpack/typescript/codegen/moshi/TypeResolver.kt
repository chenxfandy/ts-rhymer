/*
 * Copyright (C) 2018 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.laidpack.typescript.codegen.moshi

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Resolves bodyType parameters against a bodyType declaration. Use this to fill in bodyType variables with
 * their actual bodyType parameters.
 */
internal open class TypeResolver {
  open fun resolveTypeVariable(typeVariable: TypeVariableName): TypeName = typeVariable

  fun resolve(typeName: TypeName): TypeName {
    return when (typeName) {
      is ClassName -> typeName

      is ParameterizedTypeName -> {
            typeName.rawType.parameterizedBy(*(typeName.typeArguments.map { resolve(it) }.toTypedArray()))
            .asNullableIf(typeName.nullable)
      }

      is WildcardTypeName -> {
        when {
          typeName.lowerBounds.size == 1 -> {
            WildcardTypeName.supertypeOf(resolve(typeName.lowerBounds[0]))
                .asNullableIf(typeName.nullable)
          }
          typeName.upperBounds.size == 1 -> {
            WildcardTypeName.subtypeOf(resolve(typeName.upperBounds[0]))
                .asNullableIf(typeName.nullable)
          }
          else -> {
            throw IllegalArgumentException(
                "Unrepresentable wildcard bodyType. Cannot have more than one bound: $typeName")
          }
        }
      }

      is TypeVariableName -> resolveTypeVariable(typeName)

      else -> throw IllegalArgumentException("Unrepresentable bodyType: $typeName")
    }
  }
}
