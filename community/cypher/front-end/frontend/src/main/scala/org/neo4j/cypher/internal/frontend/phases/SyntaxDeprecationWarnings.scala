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
package org.neo4j.cypher.internal.frontend.phases

import org.neo4j.cypher.internal.ast.Statement
import org.neo4j.cypher.internal.ast.semantics.SemanticTable
import org.neo4j.cypher.internal.frontend.phases.CompilationPhaseTracer.CompilationPhase.DEPRECATION_WARNINGS
import org.neo4j.cypher.internal.rewriting.Deprecations
import org.neo4j.cypher.internal.util.InternalNotification

/**
 * Find deprecated Cypher constructs and generate warnings for them.
 */
case class SyntaxDeprecationWarnings(deprecations: Deprecations) extends VisitorPhase[BaseContext, BaseState] {
  override def visit(state: BaseState, context: BaseContext): Unit = {
    val warnings = findDeprecations(state.statement(), state.maybeSemanticTable)

    warnings.foreach(context.notificationLogger.log)
  }

  private def findDeprecations(statement: Statement, semanticTable: Option[SemanticTable]): Set[InternalNotification] = {

    val foundWithoutContext = statement.fold(Set.empty[InternalNotification])(
      deprecations.find.andThen(deprecation => acc => acc ++ deprecation.generateNotification())
    )

    val foundWithContext = deprecations.findWithContext(statement, semanticTable).map(_.generateNotification()).collect{case Some(n) => n}

    foundWithoutContext ++ foundWithContext
  }

  override def phase = DEPRECATION_WARNINGS
}