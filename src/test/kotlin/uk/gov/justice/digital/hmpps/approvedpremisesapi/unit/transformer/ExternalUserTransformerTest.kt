package uk.gov.justice.digital.hmpps.approvedpremisesapi.unit.transformer

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ExternalUser
import uk.gov.justice.digital.hmpps.approvedpremisesapi.factory.ExternalUserEntityFactory
import uk.gov.justice.digital.hmpps.approvedpremisesapi.transformer.ExternalUserTransformer

class ExternalUserTransformerTest {
  private val externalUserTransformer = ExternalUserTransformer()

  @Test
  fun `transforms JPA ExternalUser db entity to ExternalUser api representation`() {
    val jpaEntity = ExternalUserEntityFactory().produce()

    val expectedRepresentation = ExternalUser(
      id = jpaEntity.id,
      username = jpaEntity.username,
      origin = jpaEntity.origin,
    )

    val transformation = externalUserTransformer.transformJpaToApi(jpaEntity)

    Assertions.assertThat(transformation).isEqualTo(expectedRepresentation)
  }
}
