package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import uk.gov.bis.lite.common.redis.RedissonCache;
import uk.gov.bis.lite.common.redis.Ttl;
import uk.gov.bis.lite.permissions.service.model.LicenceResult;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.function.Function;

public class RedisLicenceServiceImpl implements LicenceService {

  private static final Function<LicenceResult, Boolean> CACHE_IF_OK = licenceResult ->
      licenceResult.getStatus() == Status.OK;

  private final LicenceServiceImpl licenceServiceImpl;
  private final RedissonCache redissonCache;
  private final Ttl getLicenceByRef;
  private final Ttl getAllLicences;
  private final Ttl getLicencesByType;

  @Inject
  public RedisLicenceServiceImpl(LicenceServiceImpl licenceServiceImpl,
                                 RedissonCache redissonCache,
                                 @Named("getLicenceByRef") Ttl getLicenceByRef,
                                 @Named("getAllLicences") Ttl getAllLicences,
                                 @Named("getLicencesByType") Ttl getLicencesByType) {
    this.licenceServiceImpl = licenceServiceImpl;
    this.redissonCache = redissonCache;
    this.getLicenceByRef = getLicenceByRef;
    this.getAllLicences = getAllLicences;
    this.getLicencesByType = getLicencesByType;
  }

  @Override
  public LicenceResult getLicenceByRef(String userId, String reference) {
    return redissonCache.get(() -> licenceServiceImpl.getLicenceByRef(userId, reference),
        CACHE_IF_OK,
        "getLicenceByRef",
        getLicenceByRef,
        userId, reference);
  }

  @Override
  public LicenceResult getAllLicences(String userId) {
    return redissonCache.get(() -> licenceServiceImpl.getAllLicences(userId),
        CACHE_IF_OK,
        "getAllLicences",
        getAllLicences,
        userId);
  }

  @Override
  public LicenceResult getLicencesByType(String userId, String type) {
    return redissonCache.get(() -> licenceServiceImpl.getLicencesByType(userId, type),
        CACHE_IF_OK,
        "getLicencesByType",
        getLicencesByType,
        userId, type);
  }
}
