package uk.gov.bis.lite.permissions.service;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import uk.gov.bis.lite.common.redis.RedissonCache;
import uk.gov.bis.lite.common.redis.Ttl;
import uk.gov.bis.lite.permissions.service.model.RegistrationResult;
import uk.gov.bis.lite.permissions.service.model.Status;

import java.util.function.Function;

public class RedisRegistrationServiceImpl implements RegistrationService {

  private static final Function<RegistrationResult, Boolean> CACHE_IF_OK = registrationResult ->
      registrationResult.getStatus() == Status.OK;

  private final RegistrationServiceImpl registrationServiceImpl;
  private final RedissonCache redissonCache;
  private final Ttl getRegistrationsTtl;
  private final Ttl getRegistrationByReferenceTtl;

  @Inject
  public RedisRegistrationServiceImpl(RegistrationServiceImpl registrationServiceImpl,
                                      RedissonCache redissonCache,
                                      @Named("getRegistrationsTtl") Ttl getRegistrationsTtl,
                                      @Named("getRegistrationByReferenceTtl") Ttl getRegistrationByReferenceTtl) {
    this.registrationServiceImpl = registrationServiceImpl;
    this.redissonCache = redissonCache;
    this.getRegistrationsTtl = getRegistrationsTtl;
    this.getRegistrationByReferenceTtl = getRegistrationByReferenceTtl;
  }

  @Override
  public RegistrationResult getRegistrations(String userId) {
    return redissonCache.get(() -> registrationServiceImpl.getRegistrations(userId),
        CACHE_IF_OK,
        "getRegistrations",
        getRegistrationsTtl,
        userId);
  }

  @Override
  public RegistrationResult getRegistrationByReference(String userId, String reference) {
    return redissonCache.get(() -> registrationServiceImpl.getRegistrationByReference(userId, reference),
        CACHE_IF_OK,
        "getRegistration",
        getRegistrationByReferenceTtl,
        userId, reference);
  }

}
