package com.veritasbible.app.study.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Query("SELECT * FROM study_sessions ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<StudySession>>

    @Query("SELECT * FROM study_sessions WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<StudySession?>

    @Query("SELECT * FROM study_sessions WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): StudySession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: StudySession)

    @Update
    suspend fun update(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM study_sessions")
    suspend fun count(): Int
}

@Dao
interface StudyObservationDao {

    @Query("SELECT * FROM study_observations WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyObservation>>

    @Query("SELECT * FROM study_observations WHERE sessionId = :sessionId AND questionKey = :questionKey LIMIT 1")
    suspend fun findByQuestion(sessionId: String, questionKey: String): StudyObservation?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(observation: StudyObservation)

    @Update
    suspend fun update(observation: StudyObservation)

    @Delete
    suspend fun delete(observation: StudyObservation)

    @Query("DELETE FROM study_observations WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface StudyDivisionDao {

    @Query("SELECT * FROM study_divisions WHERE sessionId = :sessionId ORDER BY orderIndex ASC, startChapter ASC, startVerse ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyDivision>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(division: StudyDivision)

    @Update
    suspend fun update(division: StudyDivision)

    @Query("DELETE FROM study_divisions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT MAX(orderIndex) FROM study_divisions WHERE sessionId = :sessionId")
    suspend fun maxOrderIndex(sessionId: String): Int?
}

@Dao
interface StudyMarkupDao {

    @Query("SELECT * FROM study_markups WHERE sessionId = :sessionId ORDER BY chapter ASC, verse ASC, startOffset ASC, createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyMarkup>>

    @Query("SELECT * FROM study_markups WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<String>): List<StudyMarkup>

    @Query("SELECT * FROM study_markups WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): StudyMarkup?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(markup: StudyMarkup)

    @Update
    suspend fun update(markup: StudyMarkup)

    @Query("DELETE FROM study_markups WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface StudyMarkupLinkDao {

    @Query("SELECT * FROM study_markup_links WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyMarkupLink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(link: StudyMarkupLink)

    @Query("DELETE FROM study_markup_links WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM study_markup_links WHERE sessionId = :sessionId AND fromMarkupId = :fromId AND toMarkupId = :toId AND linkType = :linkType")
    suspend fun countDuplicate(sessionId: String, fromId: String, toId: String, linkType: String): Int
}

@Dao
interface StudyCharacterTagDao {

    @Query("SELECT * FROM study_character_tags WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyCharacterTag>>

    @Query("SELECT * FROM study_character_tags WHERE sessionId = :sessionId AND targetType = :targetType AND targetId = :targetId ORDER BY createdAt ASC")
    suspend fun findForTarget(sessionId: String, targetType: String, targetId: String): List<StudyCharacterTag>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(tag: StudyCharacterTag)

    @Query("DELETE FROM study_character_tags WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM study_character_tags WHERE sessionId = :sessionId AND targetType = :targetType AND targetId = :targetId AND tag = :tag")
    suspend fun countDuplicate(sessionId: String, targetType: String, targetId: String, tag: String): Int
}

@Dao
interface StudyApplicationDao {

    @Query("SELECT * FROM study_applications WHERE sessionId = :sessionId LIMIT 1")
    fun observeBySession(sessionId: String): Flow<StudyApplication?>

    @Query("SELECT * FROM study_applications WHERE sessionId = :sessionId LIMIT 1")
    suspend fun findBySession(sessionId: String): StudyApplication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(application: StudyApplication)

    @Update
    suspend fun update(application: StudyApplication)

    @Query("DELETE FROM study_applications WHERE sessionId = :sessionId")
    suspend fun deleteBySession(sessionId: String)
}

@Dao
interface StudyInterpretationDao {

    @Query("SELECT * FROM study_interpretations WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyInterpretation>>

    @Query("SELECT * FROM study_interpretations WHERE sessionId = :sessionId AND linkedMarkupId = :markupId")
    suspend fun findByLinkedMarkup(sessionId: String, markupId: String): List<StudyInterpretation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: StudyInterpretation)

    @Update
    suspend fun update(note: StudyInterpretation)

    @Query("DELETE FROM study_interpretations WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface StudyThemeCheckDao {

    @Query("SELECT * FROM study_theme_checks WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyThemeCheck>>

    @Query("SELECT * FROM study_theme_checks WHERE sessionId = :sessionId AND checkKey = :checkKey LIMIT 1")
    suspend fun findByKey(sessionId: String, checkKey: String): StudyThemeCheck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(check: StudyThemeCheck)
}

@Dao
interface StudyPropositionDao {

    @Query("SELECT * FROM study_propositions WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyProposition>>

    @Query("SELECT * FROM study_propositions WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): StudyProposition?

    @Query("SELECT * FROM study_propositions WHERE sessionId = :sessionId AND linkedMarkupIds LIKE '%' || :markupId || '%'")
    suspend fun findByLinkedMarkup(sessionId: String, markupId: String): List<StudyProposition>

    @Query("SELECT * FROM study_propositions WHERE sessionId = :sessionId AND linkedDivisionIds LIKE '%' || :divisionId || '%'")
    suspend fun findByLinkedDivision(sessionId: String, divisionId: String): List<StudyProposition>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(proposition: StudyProposition)

    @Update
    suspend fun update(proposition: StudyProposition)

    @Query("DELETE FROM study_propositions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface StudyOutlineNodeDao {

    @Query("SELECT * FROM study_outline_nodes WHERE sessionId = :sessionId ORDER BY level ASC, orderIndex ASC, createdAt ASC")
    fun observeBySession(sessionId: String): Flow<List<StudyOutlineNode>>

    @Query("SELECT * FROM study_outline_nodes WHERE sessionId = :sessionId AND linkedDivisionId = :divisionId")
    suspend fun findByLinkedDivision(sessionId: String, divisionId: String): List<StudyOutlineNode>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: StudyOutlineNode)

    @Update
    suspend fun update(node: StudyOutlineNode)

    @Query("DELETE FROM study_outline_nodes WHERE id = :id OR parentId = :id")
    suspend fun deleteCascade(id: String)

    @Query("SELECT MAX(orderIndex) FROM study_outline_nodes WHERE sessionId = :sessionId AND parentId IS :parentId")
    suspend fun maxOrderIndex(sessionId: String, parentId: String?): Int?
}
