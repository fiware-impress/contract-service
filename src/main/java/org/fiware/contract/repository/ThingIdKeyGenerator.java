package org.fiware.contract.repository;

import io.micronaut.cache.interceptor.CacheKeyGenerator;
import io.micronaut.core.annotation.AnnotationMetadata;
import org.fiware.contract.model.Thing;

public class ThingIdKeyGenerator implements CacheKeyGenerator {
	@Override
	public Object generateKey(AnnotationMetadata annotationMetadata, Object... params) {
		if (params.length > 1) {
			throw new RuntimeException("Only single things are supported");
		}
		if (params[0] instanceof Thing) {
			return ((Thing) params[0]).getIdentifier();
		}

		throw new RuntimeException("Key generator not supported for this parameter type.");
	}
}
