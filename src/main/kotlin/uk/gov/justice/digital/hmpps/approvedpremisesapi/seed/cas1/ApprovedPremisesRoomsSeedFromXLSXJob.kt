package uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.cas1

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.getColumn
import org.jetbrains.kotlinx.dataframe.name
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.approvedpremisesapi.api.model.ServiceName
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BedEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.BedRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CharacteristicEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.CharacteristicRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PremisesEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.PremisesRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.RoomEntity
import uk.gov.justice.digital.hmpps.approvedpremisesapi.jpa.entity.RoomRepository
import uk.gov.justice.digital.hmpps.approvedpremisesapi.seed.ExcelSeedJob
import java.util.UUID

class SiteSurveyImportException(message: String) : Exception(message)

@Suppress("LongParameterList")
class ApprovedPremisesRoomsSeedFromXLSXJob(
  fileName: String,
  premisesId: UUID,
  sheetName: String,
  private val premisesRepository: PremisesRepository,
  private val roomRepository: RoomRepository,
  private val bedRepository: BedRepository,
  private val siteSurvey: SiteSurvey,
) : ExcelSeedJob(
  fileName = fileName,
  premisesId = premisesId,
  sheetName = sheetName,
) {
  private val log = LoggerFactory.getLogger(this::class.java)
  override fun processDataFrame(dataFrame: DataFrame<*>) {
    findExistingPremisesOrThrow(premisesId)

    // build valid lists for rooms, characteristics and beds
    var rooms = buildRooms(dataFrame)
    val characteristics = buildCharacteristics(dataFrame)

    // add the characteristics to the rooms
    rooms.forEach { it.characteristics.addAll(characteristics[it.code!!]!!.toList()) }

    // update the rooms
    rooms = createOrUpdateRooms(rooms)

    val beds = buildBeds(dataFrame, rooms)
    createBedsIfNotExist(beds)
  }

  private fun buildRooms(dataFrame: DataFrame<*>): MutableList<RoomEntity> {
    val rooms = mutableListOf<RoomEntity>()

    for (i in 1..<dataFrame.columnsCount()) {
      val roomAnswers = dataFrame.getColumn(i)
      val room = createRoom(roomAnswers.name, roomAnswers[0].toString())

      rooms.add(room)
    }
    return rooms
  }

  private fun buildCharacteristics(dataFrame: DataFrame<*>): MutableMap<String, MutableList<CharacteristicEntity>> {
    var premisesCharacteristics = mutableMapOf<String, MutableList<CharacteristicEntity>>()

    siteSurvey.questionToCharacterEntityMapping.forEach { (question, characteristic) ->
      val rowId = dataFrame.getColumn(0).values().indexOf(question)

      if (rowId == -1) throw SiteSurveyImportException("Characteristic question '$question' not found on sheet $sheetName.")

      for (colId in 1..<dataFrame.columnsCount()) {
        val roomCode = dataFrame.getColumn(colId).name
        val answer = dataFrame[rowId][colId].toString()

        if (answer.equals("yes", true)) {
          premisesCharacteristics.computeIfAbsent(roomCode) { mutableListOf() }.add(characteristic!!)
        } else if (!answer.equals("no", true)) {
          throw SiteSurveyImportException("Expecting 'yes' or 'no' for question '$question' but is '$answer' on sheet $sheetName.")
        }
      }
    }
    return premisesCharacteristics
  }

  private fun buildBeds(dataFrame: DataFrame<*>, rooms: MutableList<RoomEntity>): List<BedEntity> {
    val beds = mutableListOf<BedEntity>()
    for (i in 1..<dataFrame.columnsCount()) {
      val roomAnswers = dataFrame.getColumn(i)
      val bedCode = roomAnswers[1].toString()
      beds.add(
        createBed(
          "${roomAnswers.name} - $bedCode",
          bedCode,
          rooms.firstOrNull { it.code == roomAnswers.name }
            ?: throw IllegalArgumentException("Room not found with id ${roomAnswers.name} for bed $bedCode."),
        ),
      )
    }
    return beds
  }

  private fun createOrUpdateRooms(rooms: MutableList<RoomEntity>): MutableList<RoomEntity> {
    rooms.forEachIndexed { index, room ->
      var persistedRoom = roomRepository.findByCode(room.code!!)

      if (persistedRoom == null) {
        roomRepository.save(room)
        log.info("Created new room ${room.id} with code ${room.code} and name ${room.name} in premise ${room.premises.name}.")
        log.info("Added characteristic(s) ${room.characteristics.joinToString()} to room code ${room.code}.")
      } else {
        rooms[index] = persistedRoom
        persistedRoom.characteristics.clear()
        persistedRoom.characteristics.addAll(room.characteristics)
        roomRepository.save(persistedRoom)
        log.info("Added characteristic(s) ${persistedRoom.characteristics.joinToString()} to room code ${persistedRoom.code}.")
      }
    }
    return rooms
  }

  private fun createBedsIfNotExist(beds: List<BedEntity>) {
    beds.forEach {
      val existingBed = bedRepository.findByCode(it.code!!)
      if (existingBed != null) {
        log.info("Bed ${it.id} with code ${it.code} already exists in room code ${it.room.code}.")
        return
      } else {
        bedRepository.save(it)
        log.info("Created new bed ${it.id} with code ${it.code} and name ${it.name} in room code ${it.room.code}.")
      }
    }
  }

  private fun createRoom(roomCode: String, roomName: String): RoomEntity = RoomEntity(
    id = UUID.randomUUID(),
    name = roomName,
    code = roomCode,
    notes = null,
    premises = premisesRepository.findByIdOrNull(premisesId)!!,
    beds = mutableListOf(),
    characteristics = mutableListOf(),
  )

  fun createBed(bedName: String, bedCode: String, room: RoomEntity): BedEntity = BedEntity(
    id = UUID.randomUUID(),
    name = bedName,
    code = bedCode,
    room = room,
    endDate = null,
    createdAt = null,
  )

  private fun findExistingPremisesOrThrow(premisesId: UUID): PremisesEntity {
    return premisesRepository.findByIdOrNull(premisesId) ?: throw SiteSurveyImportException(
      "No premises with id '$premisesId' found.",
    )
  }
}

@Component
class SiteSurvey(characteristicRepository: CharacteristicRepository) {
  private val questionToPropertyNameMapping = mapOf(
//    "Is this bed in a single room?" to "isSingle",
//    "Is this an IAP?" to "isIAP",
//    "Is this AP a PIPE?" to "isPIPE",
//    "Is this AP an Enhanced Security AP?" to "isESAP",
//    "Is this AP semi specialist Mental Health?" to "isSemiSpecialistMentalHealth",
//    "Is this a Recovery Focussed AP?" to "isRecoveryFocussed",
//    "Is this AP suitable for people at risk of criminal exploitation? N.B Enhanced Security sites answer No, other APs answer Yes." to "isSuitableForVulnerable",
//    "Does this AP accept people who have committed sexual offences against adults?" to "acceptsSexOffenders",
//    "Does this AP accept people who have committed sexual offences against children?" to "acceptsChildSexOffenders",
//    "Does this AP accept people who have committed non-sexual offences against children?" to "acceptsNonSexualChildOffenders",
//    "Does this AP accept people who have been convicted of hate crimes?" to "acceptsHateCrimeOffenders",
//    "Is this AP Catered (no for Self Catered)?" to "isCatered",
//    "Is there a step free entrance to the AP at least 900mm wide?" to "hasWideStepFreeAccess",
//    "Are corridors leading to communal areas at least 1.2m wide?" to "hasWideAccessToCommunalAreas",
//    "Do corridors leading to communal areas have step free access?" to "hasStepFreeAccessToCommunalAreas",
//    "Does this AP have bathroom facilities that have been adapted for wheelchair users?" to "hasWheelChairAccessibleBathrooms",
//    "Is there a lift at this AP?" to "hasLift",
//    "Does this AP have tactile & directional flooring?" to "hasTactileFlooring",
//    "Does this AP have signs in braille?" to "hasBrailleSignage",
//    "Does this AP have a hearing loop?" to "hasHearingLoop",
//    "Are there any additional restrictions on people that this AP can accommodate?" to "additionalRestrictions",
//    "Is the room using only furnishings and bedding supplied by FM?" to "isFullyFm",
//    "Does this room have Crib7 rated bedding?" to "hasCrib7Bedding",
//    "Is there a smoke/heat detector in the room?" to "hasSmokeDetector",
//    "Is this room on the top floor with at least one external wall and not located directly next to a fire exit or a protected stairway?" to "isTopFloorVulnerable",
//    "Is the room close to the admin/staff office on the ground floor with at least one external wall and not located directly next to a fire exit or a protected stairway?"
//    to "isGroundFloorNrOffice",
//    "is there a water mist extinguisher in close proximity to this room?" to "hasNearbySprinkler",
//    "Is this room suitable for people who pose an arson risk? (Must answer yes to Q; 6 & 7, and 9 or  10)" to "isArsonSuitable",
//    "Is this room currently a designated arson room?" to "isArsonDesignated",
//    "If IAP - Is there any insurance conditions that prevent a person with arson convictions being placed?" to "hasArsonInsuranceConditions",
//    "Is this room suitable for people convicted of sexual offences?" to "isSuitedForSexOffenders",
//    "Does this room have en-suite bathroom facilities?" to "hasEnSuite",
//    "Are corridors leading to this room of sufficient width to accommodate a wheelchair? (at least 1.2m wide)" to "isWheelchairAccessible",
//    "Is the door to this room at least 900mm wide?" to "hasWideDoor",
//    "Is there step free access to this room and in corridors leading to this room?" to "hasStepFreeAccess",
//    "Are there fixed mobility aids in this room?" to "hasFixedMobilityAids",
//    "Does this room have at least a 1500mmx1500mm turning space?" to "hasTurningSpace",
//    "Is there provision for people to call for assistance from this room?" to "hasCallForAssistance",
//    "Can this room be designated as suitable for wheelchair users?   Must answer yes to Q23-26 on previous sheet and Q17-21 on this sheet)" to "isWheelchairDesignated",
//    "Can this room be designated as suitable for people requiring step free access? (Must answer yes to Q23 and 25 on previous sheet and Q19 on this sheet)" to "isStepFreeDesignated",
    "Is this room located on the ground floor?" to "IsGroundFloor",
  )

  val questionToCharacterEntityMapping = questionToPropertyNameMapping.map { (key, value) ->
    Pair(key, characteristicRepository.findByPropertyName(value, ServiceName.approvedPremises.value))
  }.toMap()
}
