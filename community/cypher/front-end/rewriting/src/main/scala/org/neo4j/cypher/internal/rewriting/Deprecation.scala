/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.rewriting

import org.neo4j.cypher.internal.ast
import org.neo4j.cypher.internal.ast.DeprecatedSyntax
import org.neo4j.cypher.internal.ast.ExistsConstraints
import org.neo4j.cypher.internal.ast.HasCatalog
import org.neo4j.cypher.internal.ast.NodeExistsConstraints
import org.neo4j.cypher.internal.ast.RelExistsConstraints
import org.neo4j.cypher.internal.ast.ShowConstraintsClause
import org.neo4j.cypher.internal.ast.ShowIndexesClause
import org.neo4j.cypher.internal.ast.Statement
import org.neo4j.cypher.internal.ast.semantics.SemanticTable
import org.neo4j.cypher.internal.expressions.ContainerIndex
import org.neo4j.cypher.internal.expressions.Expression
import org.neo4j.cypher.internal.expressions.ExtractExpression
import org.neo4j.cypher.internal.expressions.ExtractScope
import org.neo4j.cypher.internal.expressions.FilterExpression
import org.neo4j.cypher.internal.expressions.FunctionInvocation
import org.neo4j.cypher.internal.expressions.FunctionName
import org.neo4j.cypher.internal.expressions.ListComprehension
import org.neo4j.cypher.internal.expressions.ListLiteral
import org.neo4j.cypher.internal.expressions.Parameter
import org.neo4j.cypher.internal.expressions.ParameterWithOldSyntax
import org.neo4j.cypher.internal.expressions.PatternExpression
import org.neo4j.cypher.internal.expressions.Property
import org.neo4j.cypher.internal.expressions.PropertyKeyName
import org.neo4j.cypher.internal.expressions.RelationshipPattern
import org.neo4j.cypher.internal.expressions.SignedHexIntegerLiteral
import org.neo4j.cypher.internal.expressions.SignedOctalIntegerLiteral
import org.neo4j.cypher.internal.expressions.StringLiteral
import org.neo4j.cypher.internal.expressions.functions.Exists
import org.neo4j.cypher.internal.util.ASTNode
import org.neo4j.cypher.internal.util.DeprecatedCatalogKeywordForAdminCommandSyntax
import org.neo4j.cypher.internal.util.DeprecatedCoercionOfListToBoolean
import org.neo4j.cypher.internal.util.DeprecatedCreateIndexSyntax
import org.neo4j.cypher.internal.util.DeprecatedCreatePropertyExistenceConstraintSyntax
import org.neo4j.cypher.internal.util.DeprecatedDefaultDatabaseSyntax
import org.neo4j.cypher.internal.util.DeprecatedDefaultGraphSyntax
import org.neo4j.cypher.internal.util.DeprecatedDropConstraintSyntax
import org.neo4j.cypher.internal.util.DeprecatedDropIndexSyntax
import org.neo4j.cypher.internal.util.DeprecatedFunctionNotification
import org.neo4j.cypher.internal.util.DeprecatedHexLiteralSyntax
import org.neo4j.cypher.internal.util.DeprecatedOctalLiteralSyntax
import org.neo4j.cypher.internal.util.DeprecatedParameterSyntax
import org.neo4j.cypher.internal.util.DeprecatedPatternExpressionOutsideExistsSyntax
import org.neo4j.cypher.internal.util.DeprecatedPropertyExistenceSyntax
import org.neo4j.cypher.internal.util.DeprecatedRelTypeSeparatorNotification
import org.neo4j.cypher.internal.util.DeprecatedShowExistenceConstraintSyntax
import org.neo4j.cypher.internal.util.DeprecatedShowSchemaSyntax
import org.neo4j.cypher.internal.util.DeprecatedVarLengthBindingNotification
import org.neo4j.cypher.internal.util.Foldable.SkipChildren
import org.neo4j.cypher.internal.util.InternalNotification
import org.neo4j.cypher.internal.util.LengthOnNonPathNotification
import org.neo4j.cypher.internal.util.symbols.CTAny
import org.neo4j.cypher.internal.util.symbols.CTBoolean
import org.neo4j.cypher.internal.util.symbols.CTList

import scala.collection.immutable.TreeMap

object Deprecations {

  def propertyOf(propertyKey: String): Expression => Expression =
    e => Property(e, PropertyKeyName(propertyKey)(e.position))(e.position)

  def renameFunctionTo(newName: String): FunctionInvocation => FunctionInvocation =
    f => f.copy(functionName = FunctionName(newName)(f.functionName.position))(f.position)

  // Deprecated in 4.x
  case object V1 extends Deprecations {
    val functionRenames: Map[String, String] =
      TreeMap(
        // put any V1 deprecation here as
        // "old name" -> "new name"
      )(CaseInsensitiveOrdered)

    override val find: PartialFunction[Any, Deprecation] = {

      // straight renames
      case f@FunctionInvocation(_, FunctionName(name), _, _) if functionRenames.contains(name) =>
        Deprecation(
          () => renameFunctionTo(functionRenames(name))(f),
          () => Some(DeprecatedFunctionNotification(f.position, name, functionRenames(name)))
        )

      // old octal literal syntax
      case p@SignedOctalIntegerLiteral(stringVal) if stringVal.charAt(1) != 'o' =>
        Deprecation(
          () => p,
          () => Some(DeprecatedOctalLiteralSyntax(p.position))
        )

      // old hex literal syntax
      case p@SignedHexIntegerLiteral(stringVal) if stringVal.charAt(1) == 'X' =>
        Deprecation(
          () => SignedHexIntegerLiteral(stringVal.toLowerCase)(p.position),
          () => Some(DeprecatedHexLiteralSyntax(p.position))
        )

      // timestamp
      case f@FunctionInvocation(_, FunctionName(name), _, _) if name.equalsIgnoreCase("timestamp") =>
        Deprecation(
          () => renameFunctionTo("datetime").andThen(propertyOf("epochMillis"))(f),
          () => None
        )

      // var-length binding
      case p@RelationshipPattern(Some(variable), _, Some(_), _, _, _) =>
        Deprecation(
          () => p,
          () => Some(DeprecatedVarLengthBindingNotification(p.position, variable.name))
        )

      case i: ast.CreateIndexOldSyntax =>
        Deprecation(
          () => i,
          () => Some(DeprecatedCreateIndexSyntax(i.position))
        )

      case i: ast.DropIndex =>
        Deprecation(
          () => i,
          () => Some(DeprecatedDropIndexSyntax(i.position))
        )

      case c: ast.DropNodeKeyConstraint =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.DropUniquePropertyConstraint =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.DropNodePropertyExistenceConstraint =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.DropRelationshipPropertyExistenceConstraint =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDropConstraintSyntax(c.position))
        )

      case c: ast.CreateNodePropertyExistenceConstraint if c.oldSyntax =>
        Deprecation(
          () => c,
          () => Some(DeprecatedCreatePropertyExistenceConstraintSyntax(c.position))
        )

      case c: ast.CreateRelationshipPropertyExistenceConstraint if c.oldSyntax =>
        Deprecation(
          () => c,
          () => Some(DeprecatedCreatePropertyExistenceConstraintSyntax(c.position))
        )

      case e@Exists(Property(_, _)) =>
        Deprecation(
          () => e,
          () => Some(DeprecatedPropertyExistenceSyntax(e.position))
        )

      case e@Exists(ContainerIndex(_, _)) =>
        Deprecation(
          () => e,
          () => Some(DeprecatedPropertyExistenceSyntax(e.position))
        )

      case s: ShowIndexesClause if s.verbose || s.brief =>
        Deprecation(
          () => s,
          () => Some(DeprecatedShowSchemaSyntax(s.position))
        )

      case c@ast.GrantPrivilege(ast.DatabasePrivilege(_, List(ast.DefaultDatabaseScope())), _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDefaultDatabaseSyntax(c.position))
        )

      case c@ast.DenyPrivilege(ast.DatabasePrivilege(_, List(ast.DefaultDatabaseScope())), _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDefaultDatabaseSyntax(c.position))
        )

      case c@ast.RevokePrivilege(ast.DatabasePrivilege(_, List(ast.DefaultDatabaseScope())), _, _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDefaultDatabaseSyntax(c.position))
        )

      case c@ast.GrantPrivilege(ast.GraphPrivilege(_, List(ast.DefaultGraphScope())), _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDefaultGraphSyntax(c.position))
        )

      case c@ast.DenyPrivilege(ast.GraphPrivilege(_, List(ast.DefaultGraphScope())), _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDefaultGraphSyntax(c.position))
        )

      case c@ast.RevokePrivilege(ast.GraphPrivilege(_, List(ast.DefaultGraphScope())), _, _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedDefaultGraphSyntax(c.position))
        )

      case c@ShowConstraintsClause(_, ExistsConstraints(DeprecatedSyntax), _, _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedShowExistenceConstraintSyntax(c.position))
        )

      case c@ShowConstraintsClause(_, NodeExistsConstraints(DeprecatedSyntax), _, _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedShowExistenceConstraintSyntax(c.position))
        )

      case c@ShowConstraintsClause(_, RelExistsConstraints(DeprecatedSyntax), _, _, _, _) =>
        Deprecation(
          () => c,
          () => Some(DeprecatedShowExistenceConstraintSyntax(c.position))
        )

      case s: ShowConstraintsClause if s.verbose || s.brief =>
        Deprecation(
          () => s,
          () => Some(DeprecatedShowSchemaSyntax(s.position))
        )

      case c: HasCatalog =>
        Deprecation(
          () => c.source,
          () => Some(DeprecatedCatalogKeywordForAdminCommandSyntax(c.position))
        )
    }
  }

  case object V2 extends Deprecations {
    override val find: PartialFunction[Any, Deprecation] = V1.find
  }

  case object deprecatedFeaturesIn4_3AfterRewrite extends Deprecations {
    override def find: PartialFunction[Any, Deprecation] = PartialFunction.empty

    override def findWithContext(statement: Statement, semanticTable: Option[SemanticTable]): Set[Deprecation] = {
      val deprecationsOfPatternExpressionsOutsideExists = statement.treeFold[Set[Deprecation]](Set.empty) {
        case FunctionInvocation(_, FunctionName(name), _, _) if name.toLowerCase.equals("exists") =>
          // Don't look inside exists()
          deprecations => SkipChildren(deprecations)

        case p: PatternExpression =>
          val deprecation = Deprecation(
            () => p,
            () => Some(DeprecatedPatternExpressionOutsideExistsSyntax(p.position))
          )
          deprecations => SkipChildren(deprecations + deprecation)
      }

      val deprecationsOfCoercingListToBoolean = semanticTable.map { table =>
          def isListCoercedToBoolean(e: Expression) = table.types.get(e).exists(
            typeInfo => CTList(CTAny).covariant.containsAll(typeInfo.specified) && CTBoolean.covariant.containsAll(typeInfo.actual)
          )

          statement.treeFold[Set[Deprecation]](Set.empty) {
            case e: Expression if isListCoercedToBoolean(e) =>
              val deprecation = Deprecation(
                () => e,
                () => Some(DeprecatedCoercionOfListToBoolean(e.position))
              )
              deprecations => SkipChildren(deprecations + deprecation)
          }
      }.getOrElse(Set.empty)

      deprecationsOfPatternExpressionsOutsideExists ++ deprecationsOfCoercingListToBoolean
    }
  }

  // This is functionality that has been removed in an earlier version of 4.x but still should work (but be deprecated) when using CYPHER 3.5
  case object removedFeaturesIn4_x extends Deprecations {
    val removedFunctionsRenames: Map[String, String] =
      TreeMap(
        "toInt" -> "toInteger",
        "upper" -> "toUpper",
        "lower" -> "toLower",
        "rels" -> "relationships"
      )(CaseInsensitiveOrdered)

    override def find: PartialFunction[Any, Deprecation] = {

      case f@FunctionInvocation(_, FunctionName(name), _, _) if removedFunctionsRenames.contains(name) =>
        Deprecation(
          () => renameFunctionTo(removedFunctionsRenames(name))(f),
          () => Some(DeprecatedFunctionNotification(f.position, name, removedFunctionsRenames(name)))
        )

      // extract => list comprehension
      case e@ExtractExpression(scope, expression) =>
        Deprecation(
          () => ListComprehension(scope, expression)(e.position),
          () => Some(DeprecatedFunctionNotification(e.position, "extract(...)", "[...]"))
        )

      // filter => list comprehension
      case e@FilterExpression(scope, expression) =>
        Deprecation(
          () => ListComprehension(ExtractScope(scope.variable, scope.innerPredicate, None)(scope.position), expression)(e.position),
          () => Some(DeprecatedFunctionNotification(e.position, "filter(...)", "[...]"))
        )

      // old parameter syntax
      case p@ParameterWithOldSyntax(name, parameterType) =>
        Deprecation(
          () => Parameter(name, parameterType)(p.position),
          () => Some(DeprecatedParameterSyntax(p.position))
        )

      // length of a string, collection or pattern expression
      case f@FunctionInvocation(_, FunctionName(name), _, args)
        if name.toLowerCase.equals("length") && args.nonEmpty &&
          (args.head.isInstanceOf[StringLiteral] || args.head.isInstanceOf[ListLiteral] || args.head.isInstanceOf[PatternExpression]) =>
        Deprecation(
          () => renameFunctionTo("size")(f),
          () => Some(LengthOnNonPathNotification(f.position))
        )

      // legacy type separator
      case p@RelationshipPattern(variable, _, length, properties, _, true) if variable.isDefined || length.isDefined || properties.isDefined =>
        Deprecation(
          () => p.copy(legacyTypeSeparator = false)(p.position),
          () => Some(DeprecatedRelTypeSeparatorNotification(p.position))
        )
    }
  }

  // This is functionality that has been removed in 4.3 but still should work (but be deprecated) when using CYPHER 3.5 or CYPHER 4.2
  case object removedFeaturesIn4_3 extends Deprecations {
    val removedFunctionsRenames: Map[String, String] =
      TreeMap()(CaseInsensitiveOrdered)

    override def find: PartialFunction[Any, Deprecation] = {

      case f@FunctionInvocation(_, FunctionName(name), _, _) if removedFunctionsRenames.contains(name) =>
        Deprecation(
          () => renameFunctionTo(removedFunctionsRenames(name))(f),
          () => Some(DeprecatedFunctionNotification(f.position, name, removedFunctionsRenames(name)))
        )
    }
  }

  object CaseInsensitiveOrdered extends Ordering[String] {
    def compare(x: String, y: String): Int =
      x.compareToIgnoreCase(y)
  }
}

/**
 * One deprecation.
 *
 * This class holds both the ability to replace a part of the AST with the preferred non-deprecated variant, and
 * the ability to generate an optional notification to the user that they are using a deprecated feature.
 *
 * @param generateReplacement  function which rewrites the matched construct
 * @param generateNotification function which generates an appropriate deprecation notification
 */
case class Deprecation(generateReplacement: () => ASTNode, generateNotification: () => Option[InternalNotification])

trait Deprecations extends {
  def find: PartialFunction[Any, Deprecation]
  def findWithContext(statement: Statement, semanticTable: Option[SemanticTable]): Set[Deprecation] = Set.empty
}
