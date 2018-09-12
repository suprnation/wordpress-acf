package com.suprnation.cms.service

import com.suprnation.cms.enums.CmsPostStatus
import com.suprnation.cms.model.CmsPost
import com.suprnation.cms.repository.CmsPostRepository
import org.joda.time.{DateTime, DateTimeZone}
import org.junit.runner.RunWith
import org.junit.{Assert, Before, Test}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

@RunWith(classOf[SpringJUnit4ClassRunner])
@ContextConfiguration(locations = Array("classpath:test-applicationContext.xml"))
class CmsPostServiceTest() {

  @Autowired
  private var cmsPostRepository: CmsPostRepository = _

  private var cmsPostService: CmsPostService = _

  private val postName = "postName"

  private val status = CmsPostStatus.publish.name()

  @Before
  def before(): Unit = {
    cmsPostService = new CmsPostServiceImpl(cmsPostRepository)
    cmsPostRepository.save(new CmsPost(1L, "2018-09-12 10:00:00", postName, status))
  }

  @Test
  def testGteFilterDateEqual(): Unit = {

    val date = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateGte(postName, date)
    Assert.assertEquals(1, posts.size)

  }

  @Test
  def testGteFilterDateBefore(): Unit = {

    val date = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(9)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateGte(postName, date)
    Assert.assertEquals(1, posts.size)

  }

  @Test
  def testGteFilterDateAfter(): Unit = {

    val date = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(11)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateGte(postName, date)
    Assert.assertEquals(0, posts.size)

  }

  @Test
  def testLteFilterDateEqual(): Unit = {

    val date = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateLte(postName, date)
    Assert.assertEquals(1, posts.size)

  }

  @Test
  def testLteFilterDateBefore(): Unit = {

    val date = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(9)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateLte(postName, date)
    Assert.assertEquals(0, posts.size)

  }

  @Test
  def testLteFilterDateAfter(): Unit = {

    val date = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(11)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateLte(postName, date)
    Assert.assertEquals(1, posts.size)

  }

  @Test
  def testGteAndLteFilterDateBetween(): Unit = {

    val start = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val end = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateGteAndModifiedDateLte(postName, start, end)
    Assert.assertEquals(1, posts.size)

  }

  @Test
  def testGteAndLteFilterEndDateBoundary(): Unit = {

    val start = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(10)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val end = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateGteAndModifiedDateLte(postName, start, end)
    Assert.assertEquals(1, posts.size)

  }

  @Test
  def testGteAndLteFilterStartDateBoundary(): Unit = {

    val start = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(12)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val end = new DateTime(DateTimeZone.UTC)
      .withYear(2018)
      .withMonthOfYear(9)
      .withDayOfMonth(15)
      .withHourOfDay(10)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)

    val posts: Iterable[CmsPost] = cmsPostService.findByTypeAndModifiedDateGteAndModifiedDateLte(postName, start, end)
    Assert.assertEquals(1, posts.size)

  }

}
